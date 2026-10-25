package com.example.interactive.shared.sample

import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.vehicle.LatLng
import com.example.interactive.shared.vehicle.ReplayPoint

/** 동 목록의 한 항목. 코드는 7단계에서 실제 행정동 경계 데이터의 코드로 바꾼다 */
data class SampleDong(val code: String, val name: String, val center: LatLng)

data class SampleFriend(val id: String, val name: String)

data class SampleTrip(val trip: Trip, val stops: List<Stop>)

/** assets/sample_data.json 내용. 시연·직접 조작 두 모드가 같이 쓴다 (저장은 4단계) */
data class SampleData(
    val dongs: List<SampleDong>,
    val friends: List<SampleFriend>,
    val replayTimeline: List<ReplayPoint>,
    val pastTrips: List<SampleTrip>,
    val territories: List<Territory>,
)
