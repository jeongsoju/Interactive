package com.example.interactive.shared.record

import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.VehicleState
import java.time.Instant
import java.time.ZoneId

/** 기록하다가 일어난 일. 영토 갱신(8단계)·P 카드(10단계)가 이걸 받아 쓴다 */
sealed interface TripEvent {
    data class TripStarted(val trip: Trip) : TripEvent
    data class StopStarted(val stop: Stop) : TripEvent

    /** P로 체류 최소 시간(10분)이 된 순간. 한 정차에 한 번만 나온다 */
    data class StayConfirmed(val stop: Stop) : TripEvent

    /** kept가 false면 10분 미만 '잠깐 멈춤'이라 버려진 것 */
    data class StopEnded(val stop: Stop, val kept: Boolean) : TripEvent
    data class TripEnded(val trip: Trip) : TripEvent
}

/**
 * 차량 상태를 받아 여행·정차를 기록한다 (docs/spec.md F1). 저장은 4단계라 메모리에만 둔다.
 * 시간 판단은 모두 VehicleState.nowMillis를 쓰고, 날짜가 바뀌었는지만 zone(기기 시간대)으로 본다.
 * N(중립)은 무시한다.
 */
class TripRecorder(private val zone: ZoneId = ZoneId.systemDefault()) {

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
    private var nextTripId = 1L
    private var nextStopId = 1L

    /** 차량 상태가 바뀔 때마다 불러 준다. 이번에 일어난 일을 돌려준다 */
    fun onState(state: VehicleState): List<TripEvent> {
        val events = mutableListOf<TripEvent>()
        checkTime(state, events)
        if (state.gear != Gear.N && state.gear != lastGear) {
            lastGear = state.gear
            changeGear(state, events)
        }
        return events
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
            fuelCostWon = null,
            isFuelCostEstimated = true,
            route = emptyList(),
        )
        trips = trips + trip
        tripActive = true
        parkOdometerKm = state.odometerKm
        events += TripEvent.TripStarted(trip)
    }

    private fun startStop(state: VehicleState, events: MutableList<TripEvent>) {
        val stop = Stop(
            id = nextStopId++,
            tripId = trips.last().id,
            latitude = state.location?.latitude,
            longitude = state.location?.longitude,
            dongCode = null, // 위치→동 변환은 7단계
            startMillis = state.nowMillis,
            endMillis = null,
            occupants = state.occupants,
        )
        stops = stops + stop
        openStop = stop
        stayConfirmed = false
        parkOdometerKm = state.odometerKm
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
        events += TripEvent.StopEnded(closed, kept)
    }

    // 여행의 끝 시각·끝 주행거리는 마지막으로 P가 된 때의 값
    private fun endTrip(lastParkMillis: Long, events: MutableList<TripEvent>) {
        val last = trips.last()
        val ended = last.copy(endMillis = lastParkMillis, endOdometerKm = parkOdometerKm)
        trips = trips.dropLast(1) + ended
        tripActive = false
        events += TripEvent.TripEnded(ended)
    }

    private fun dateOf(millis: Long) = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    private companion object {
        const val HOUR_MILLIS = 60 * 60_000L
    }
}
