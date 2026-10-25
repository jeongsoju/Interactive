package com.example.interactive.shared.record

import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.sample.SampleTrip
import com.example.interactive.shared.vehicle.LatLng

/** 기록 탭에 보일 여행 하나: 여행 + 그 여행의 정차들. key는 목록에서 고른 여행을 가리키는 값(샘플·기록의 id가 겹쳐서 따로 만든다) */
data class TripEntry(val key: String, val trip: Trip, val stops: List<Stop>, val live: Boolean) {

    /** 지도에 그릴 길. 경로 점이 2개 이상이면 그것을, 아니면(지난 샘플 여행) 정차 위치를 순서대로 잇는다 */
    fun path(): List<LatLng> =
        if (trip.route.size >= 2) trip.route.map { LatLng(it.latitude, it.longitude) }
        else stops.sortedBy { it.startMillis }.mapNotNull { s -> s.latitude?.let { LatLng(it, s.longitude ?: return@mapNotNull null) } }

    /** 정차 지점들의 위치 (지도 점) */
    fun stopPoints(): List<LatLng> =
        stops.sortedBy { it.startMillis }.mapNotNull { s -> s.latitude?.let { LatLng(it, s.longitude ?: return@mapNotNull null) } }
}

object TripEntries {
    /** 샘플 지난 여행과 지금까지 기록한 여행을 합쳐 최근 것이 앞에 오게 한다 */
    fun build(sample: List<SampleTrip>, trips: List<Trip>, stops: List<Stop>): List<TripEntry> {
        val past = sample.map { TripEntry("sample-${it.trip.id}", it.trip, it.stops, live = false) }
        val recorded = trips.map { t ->
            TripEntry("rec-${t.id}", t, stops.filter { it.tripId == t.id }, live = t.endMillis == null)
        }
        return (past + recorded).sortedByDescending { it.trip.startMillis }
    }
}
