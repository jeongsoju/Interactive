package com.example.interactive

import android.content.Context
import com.example.interactive.shared.model.Dong
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.sample.SampleData
import com.example.interactive.shared.sample.SampleFriend
import com.example.interactive.shared.sample.SampleTrip
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.LatLng
import com.example.interactive.shared.vehicle.ReplayPoint
import org.json.JSONArray
import org.json.JSONObject

private const val DAY_MILLIS = 24 * 60 * 60_000L
private const val MINUTE_MILLIS = 60_000L

/** assets/sample_data.json을 읽는다. 과거 여행의 날짜는 nowMillis 기준으로 계산한다 */
object SampleDataLoader {

    fun load(context: Context, allDongs: List<Dong>, nowMillis: Long): SampleData {
        val text = context.assets.open("sample_data.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)

        val allByCode = allDongs.associateBy { it.code }
        val codes = root.getJSONArray("dongs")
        val dongs = List(codes.length()) { allByCode.getValue(codes.getString(it)) }
        val dongByCode = dongs.associateBy { it.code }

        val friends = root.getJSONArray("friends").map { SampleFriend(it.getString("id"), it.getString("name")) }

        val replay = root.getJSONArray("replay").map {
            ReplayPoint(
                tSec = it.getLong("tMin") * 60,
                gear = Gear.valueOf(it.getString("gear")),
                location = dongByCode.getValue(it.getString("dong")).center,
                odometerKm = it.getDouble("odometerKm"),
                occupants = it.getInt("occupants"),
            )
        }

        var stopId = 1L
        val pastTrips = root.getJSONArray("pastTrips").mapIndexed { index, t ->
            val tripId = index + 1L
            val start = nowMillis - t.getLong("daysAgo") * DAY_MILLIS
            val trip = Trip(
                id = tripId,
                startMillis = start,
                endMillis = start + t.getLong("durationMinutes") * MINUTE_MILLIS,
                startOdometerKm = t.getDouble("startOdometerKm"),
                endOdometerKm = t.getDouble("endOdometerKm"),
                fuelCostWon = t.getInt("fuelCostWon"),
                isFuelCostEstimated = true,
                route = emptyList(),
            )
            val stops = t.getJSONArray("stops").map {
                val dong = dongByCode.getValue(it.getString("dong"))
                val stopStart = start + it.getLong("startMinute") * MINUTE_MILLIS
                Stop(
                    id = stopId++,
                    tripId = tripId,
                    latitude = dong.center.latitude,
                    longitude = dong.center.longitude,
                    dongCode = dong.code,
                    startMillis = stopStart,
                    endMillis = stopStart + it.getLong("stayMinutes") * MINUTE_MILLIS,
                    occupants = it.getInt("occupants"),
                )
            }
            SampleTrip(trip, stops)
        }

        val territories = root.getJSONArray("territories").map {
            val scores = it.getJSONObject("scores")
            Territory(
                dongCode = it.getString("dong"),
                ownerId = it.optString("ownerId").ifEmpty { null },
                scores = scores.keys().asSequence().associateWith { id -> scores.getInt(id) },
            )
        }

        return SampleData(dongs, friends, replay, pastTrips, territories)
    }

    private inline fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
        List(length()) { transform(getJSONObject(it)) }

    private inline fun <T> JSONArray.mapIndexed(transform: (Int, JSONObject) -> T): List<T> =
        List(length()) { transform(it, getJSONObject(it)) }
}
