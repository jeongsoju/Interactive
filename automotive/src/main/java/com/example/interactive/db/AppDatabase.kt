package com.example.interactive.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.interactive.shared.model.RoutePoint
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Trip

@Entity(tableName = "trip")
data class TripRow(
    @PrimaryKey val id: Long,
    val startMillis: Long,
    val endMillis: Long?,
    val startOdometerKm: Double,
    val endOdometerKm: Double?,
    val startBatteryPercent: Int?,
    val endBatteryPercent: Int?,
)

@Entity(tableName = "stop")
data class StopRow(
    @PrimaryKey val id: Long,
    val tripId: Long,
    val latitude: Double?,
    val longitude: Double?,
    val dongCode: String?,
    val startMillis: Long,
    val endMillis: Long?,
    val occupants: Int,
)

@Entity(tableName = "route_point", indices = [Index("tripId")])
data class RoutePointRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val timeMillis: Long,
    val latitude: Double,
    val longitude: Double,
)

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrip(trip: TripRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStop(stop: StopRow)

    @Insert
    suspend fun addRoutePoint(point: RoutePointRow)

    @Query("DELETE FROM stop WHERE id = :id")
    suspend fun deleteStop(id: Long)

    @Query("SELECT * FROM trip ORDER BY id")
    suspend fun trips(): List<TripRow>

    @Query("SELECT * FROM stop ORDER BY id")
    suspend fun stops(): List<StopRow>

    @Query("SELECT * FROM route_point ORDER BY timeMillis")
    suspend fun routePoints(): List<RoutePointRow>

    @Query("DELETE FROM trip")
    suspend fun clearTrips()

    @Query("DELETE FROM stop")
    suspend fun clearStops()

    @Query("DELETE FROM route_point")
    suspend fun clearRoutePoints()
}

@Database(
    entities = [TripRow::class, StopRow::class, RoutePointRow::class],
    // 2: 연료비(원) 대신 배터리 %를 저장. 제출 전 개발 기록뿐이라 옛 기록은 지우고 새로 만든다
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun records(): RecordDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "interactive.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}

/** 저장된 여행·정차를 읽어 온다 (경로 점은 여행마다 붙인다) */
suspend fun RecordDao.loadAll(): Pair<List<Trip>, List<Stop>> {
    val points = routePoints().groupBy { it.tripId }
    val trips = trips().map { t ->
        Trip(
            id = t.id, startMillis = t.startMillis, endMillis = t.endMillis,
            startOdometerKm = t.startOdometerKm, endOdometerKm = t.endOdometerKm,
            startBatteryPercent = t.startBatteryPercent, endBatteryPercent = t.endBatteryPercent,
            route = points[t.id].orEmpty().map { RoutePoint(it.timeMillis, it.latitude, it.longitude) },
        )
    }
    val stops = stops().map { s ->
        Stop(
            id = s.id, tripId = s.tripId, latitude = s.latitude, longitude = s.longitude,
            dongCode = s.dongCode, startMillis = s.startMillis, endMillis = s.endMillis,
            occupants = s.occupants,
        )
    }
    return trips to stops
}

suspend fun RecordDao.save(trip: Trip) = saveTrip(
    TripRow(
        trip.id, trip.startMillis, trip.endMillis, trip.startOdometerKm, trip.endOdometerKm,
        trip.startBatteryPercent, trip.endBatteryPercent,
    )
)

suspend fun RecordDao.save(stop: Stop) = saveStop(
    StopRow(
        stop.id, stop.tripId, stop.latitude, stop.longitude, stop.dongCode,
        stop.startMillis, stop.endMillis, stop.occupants,
    )
)

suspend fun RecordDao.clearAll() {
    clearTrips()
    clearStops()
    clearRoutePoints()
}
