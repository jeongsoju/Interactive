package com.example.interactive.shared.model

/** 주행 중 저장하는 경로 점 하나 */
data class RoutePoint(
    val timeMillis: Long,
    val latitude: Double,
    val longitude: Double,
)

/** 여행 하나. 첫 D에서 시작하고, 마지막 P가 오래되면 끝난다 */
data class Trip(
    val id: Long,
    val startMillis: Long,
    val endMillis: Long?,
    val startOdometerKm: Double,
    val endOdometerKm: Double?,
    val fuelCostWon: Int?,
    val isFuelCostEstimated: Boolean,
    val route: List<RoutePoint>,
) {
    /** 여행 거리. 끝나지 않은 여행은 지금 누적 주행거리를 넘겨 받아 계산한다 */
    fun distanceKm(currentOdometerKm: Double? = null): Double =
        ((endOdometerKm ?: currentOdometerKm ?: startOdometerKm) - startOdometerKm).coerceAtLeast(0.0)
}
