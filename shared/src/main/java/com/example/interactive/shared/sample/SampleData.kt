package com.example.interactive.shared.sample

import com.example.interactive.shared.model.Dong
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.vehicle.ReplayPoint


data class SampleFriend(val id: String, val name: String)

data class SampleTrip(val trip: Trip, val stops: List<Stop>)

/** assets/sample_data.json 내용. dongs는 시연에 쓰는 동(경계 데이터에서 골라 온 것). 시연·직접 조작 두 모드가 같이 쓴다 */
data class SampleData(
    val dongs: List<Dong>,
    val friends: List<SampleFriend>,
    val replayTimeline: List<ReplayPoint>,
    val pastTrips: List<SampleTrip>,
    val territories: List<Territory>,
)
