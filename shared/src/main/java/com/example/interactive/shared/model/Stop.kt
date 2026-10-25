package com.example.interactive.shared.model

import com.example.interactive.shared.AppConfig

/** 기어 P부터 D/R까지의 정차 하나. 위치를 못 읽으면 위도·경도·동 코드가 null('위치 없음') */
data class Stop(
    val id: Long,
    val tripId: Long,
    val latitude: Double?,
    val longitude: Double?,
    val dongCode: String?,
    val startMillis: Long,
    val endMillis: Long?,
    val occupants: Int,
) {
    /** 머문 시간(분). 아직 정차 중이면 지금 시각을 넘겨 받아 계산한다 */
    fun stayedMinutes(nowMillis: Long): Long =
        ((endMillis ?: nowMillis) - startMillis).coerceAtLeast(0L) / 60_000L

    /** 체류 최소 시간을 채웠는지 */
    fun isStay(nowMillis: Long): Boolean = stayedMinutes(nowMillis) >= AppConfig.STAY_MIN_MINUTES

    /** 영토 계산에 넣을 수 있는지 (위치 없음은 제외) */
    val hasLocation: Boolean get() = dongCode != null
}
