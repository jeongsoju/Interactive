package com.example.interactive.shared.vehicle

enum class Gear { P, R, N, D }

data class LatLng(val latitude: Double, val longitude: Double)

/**
 * 차량 데이터 한 장면. 앱의 시간 판단(체류 10분, 여행 끝 등)은 기기 시계가 아니라
 * 여기의 nowMillis만 쓴다. 시연 모드에서는 재생 속도에 맞춰 빨리 흐른다.
 */
data class VehicleState(
    val gear: Gear,
    val location: LatLng?,
    val odometerKm: Double,
    val occupants: Int,
    val nowMillis: Long,
)

/** 차량 데이터가 흘러나오는 통로. 시연(Replay)·직접 조작(Manual)·실제 차(Real, 6단계)가 구현한다 */
interface VehicleDataSource {
    val state: VehicleState

    /** 값이 바뀔 때마다 불린다. null이면 해제 */
    fun setOnChange(listener: ((VehicleState) -> Unit)?)
}
