package com.example.interactive.shared.record

import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.model.RoutePoint
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.LatLng
import com.example.interactive.shared.vehicle.VehicleState
import java.time.Instant
import java.time.ZoneId

/** 기록하다가 일어난 일. 영토 갱신(8단계)·P 카드(10단계)가 이걸 받아 쓴다 */
sealed interface TripEvent {
    data class TripStarted(val trip: Trip) : TripEvent
    data class StopStarted(val stop: Stop) : TripEvent

    /** 주행 중 1분 간격 경로 점이 하나 추가됐다 */
    data class RoutePointAdded(val tripId: Long, val point: RoutePoint) : TripEvent

    /** P로 체류 최소 시간(10분)이 된 순간. 한 정차에 한 번만 나온다 */
    data class StayConfirmed(val stop: Stop) : TripEvent

    /** kept가 false면 10분 미만 '잠깐 멈춤'이라 버려진 것 */
    data class StopEnded(val stop: Stop, val kept: Boolean) : TripEvent
    data class TripEnded(val trip: Trip) : TripEvent
}

/**
 * 차량 상태를 받아 여행·정차를 기록한다 (docs/spec.md F1). 저장은 이 클래스 밖(4단계 Room)에서 하고,
 * 앱을 켤 때 restore로 저장분을 되돌려 받는다.
 * 시간 판단은 모두 VehicleState.nowMillis를 쓰고, 날짜가 바뀌었는지만 zone(기기 시간대)으로 본다.
 * N(중립)은 무시한다.
 */
class TripRecorder(
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val dongCodeAt: (LatLng) -> String? = { null },
) {

    /** 여행 목록. 진행 중인 여행이 있으면 마지막이 그것이다 */
    var trips: List<Trip> = emptyList()
        private set

    /** 기록된 정차(체류로 확정됐거나 아직 진행 중인 것). 진행 중인 정차는 endMillis가 null */
    var stops: List<Stop> = emptyList()
        private set

    /** 10분이 안 돼서 버려진 정차 */
    var discardedStops: List<Stop> = emptyList()
        private set

    private var lastGear: Gear? = null
    private var tripActive = false
    private var openStop: Stop? = null
    private var stayConfirmed = false
    private var parkOdometerKm = 0.0
    private var parkBatteryPercent: Int? = null
    private var lastRouteMillis = 0L
    private var syncParkOdometer = false
    private var nextTripId = 1L
    private var nextStopId = 1L

    /** 저장돼 있던 기록을 되돌린다. 첫 상태를 받기 전에 한 번만 부른다 */
    fun restore(savedTrips: List<Trip>, savedStops: List<Stop>) {
        trips = savedTrips
        stops = savedStops
        nextTripId = (savedTrips.maxOfOrNull { it.id } ?: 0L) + 1
        nextStopId = (savedStops.maxOfOrNull { it.id } ?: 0L) + 1
        val last = savedTrips.lastOrNull()?.takeIf { it.endMillis == null } ?: return
        tripActive = true
        lastRouteMillis = last.route.lastOrNull()?.timeMillis ?: last.startMillis
        parkOdometerKm = last.startOdometerKm
        openStop = savedStops.lastOrNull { it.tripId == last.id && it.endMillis == null }
        // 꺼지기 전 기어를 짐작해서, 같은 정차가 두 번 시작되지 않게 한다
        lastGear = if (openStop != null) Gear.P else Gear.D
        syncParkOdometer = openStop != null
    }

    /** 차량 상태가 바뀔 때마다 불러 준다. 이번에 일어난 일을 돌려준다 */
    fun onState(state: VehicleState): List<TripEvent> {
        val events = mutableListOf<TripEvent>()
        if (syncParkOdometer) {
            parkOdometerKm = state.odometerKm // 복원 직후: 주차 중이니 지금 값이 곧 주차 때 값
            parkBatteryPercent = state.batteryPercent
            syncParkOdometer = false
        }
        checkTime(state, events)
        if (state.gear != Gear.N && state.gear != lastGear) {
            lastGear = state.gear
            changeGear(state, events)
        }
        addRoutePoint(state, events)
        return events
    }

    // 달리는 중(여행 중이고 정차가 없을 때)에만 1분마다 위치를 남긴다
    private fun addRoutePoint(state: VehicleState, events: MutableList<TripEvent>) {
        val location = state.location ?: return
        if (!tripActive || openStop != null) return
        if (state.nowMillis - lastRouteMillis < AppConfig.LOCATION_INTERVAL_SECONDS * 1_000L) return
        val point = RoutePoint(state.nowMillis, location.latitude, location.longitude)
        val trip = trips.last()
        trips = trips.dropLast(1) + trip.copy(route = trip.route + point)
        lastRouteMillis = state.nowMillis
        events += TripEvent.RoutePointAdded(trip.id, point)
    }

    // 시간이 흘러서 생기는 일: 체류 확정, 여행 끝. 기어 변화보다 먼저 따진다
    private fun checkTime(state: VehicleState, events: MutableList<TripEvent>) {
        val stop = openStop ?: return
        if (!stayConfirmed && stop.isStay(state.nowMillis)) {
            stayConfirmed = true
            events += TripEvent.StayConfirmed(stop)
        }
        val idleMillis = state.nowMillis - stop.startMillis
        val dateChanged = dateOf(state.nowMillis) != dateOf(stop.startMillis)
        if (idleMillis >= AppConfig.TRIP_END_IDLE_HOURS * HOUR_MILLIS || dateChanged) {
            closeOpenStop(state, events)
            endTrip(stop.startMillis, events)
        }
    }

    private fun changeGear(state: VehicleState, events: MutableList<TripEvent>) {
        when (state.gear) {
            Gear.D, Gear.R -> {
                closeOpenStop(state, events)
                if (state.gear == Gear.D && !tripActive) startTrip(state, events)
            }
            Gear.P -> if (tripActive) startStop(state, events)
            Gear.N -> {}
        }
    }

    private fun startTrip(state: VehicleState, events: MutableList<TripEvent>) {
        val trip = Trip(
            id = nextTripId++,
            startMillis = state.nowMillis,
            endMillis = null,
            startOdometerKm = state.odometerKm,
            endOdometerKm = null,
            startBatteryPercent = state.batteryPercent,
            endBatteryPercent = null,
            route = emptyList(),
        )
        trips = trips + trip
        tripActive = true
        parkOdometerKm = state.odometerKm
        parkBatteryPercent = state.batteryPercent
        lastRouteMillis = 0L // 시작하자마자 첫 점을 남긴다
        events += TripEvent.TripStarted(trip)
    }

    private fun startStop(state: VehicleState, events: MutableList<TripEvent>) {
        val stop = Stop(
            id = nextStopId++,
            tripId = trips.last().id,
            latitude = state.location?.latitude,
            longitude = state.location?.longitude,
            dongCode = state.location?.let(dongCodeAt),
            startMillis = state.nowMillis,
            endMillis = null,
            occupants = state.occupants,
        )
        stops = stops + stop
        openStop = stop
        stayConfirmed = false
        parkOdometerKm = state.odometerKm
        parkBatteryPercent = state.batteryPercent
        events += TripEvent.StopStarted(stop)
    }

    private fun closeOpenStop(state: VehicleState, events: MutableList<TripEvent>) {
        val stop = openStop ?: return
        val closed = stop.copy(endMillis = state.nowMillis)
        val kept = closed.isStay(state.nowMillis)
        stops = stops.dropLast(1)
        if (kept) stops = stops + closed else discardedStops = discardedStops + closed
        openStop = null
        stayConfirmed = false
        lastRouteMillis = state.nowMillis // 출발 위치는 정차 기록에 있으니 다음 점은 1분 뒤
        events += TripEvent.StopEnded(closed, kept)
    }

    // 여행의 끝 시각·끝 주행거리·끝 배터리는 마지막으로 P가 된 때의 값
    private fun endTrip(lastParkMillis: Long, events: MutableList<TripEvent>) {
        val last = trips.last()
        val ended = last.copy(endMillis = lastParkMillis, endOdometerKm = parkOdometerKm, endBatteryPercent = parkBatteryPercent)
        trips = trips.dropLast(1) + ended
        tripActive = false
        events += TripEvent.TripEnded(ended)
    }

    private fun dateOf(millis: Long) = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    private companion object {
        const val HOUR_MILLIS = 60 * 60_000L
    }
}
