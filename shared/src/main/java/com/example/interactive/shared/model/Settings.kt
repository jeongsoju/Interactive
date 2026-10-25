package com.example.interactive.shared.model

import com.example.interactive.shared.AppConfig

/** 사용자 설정. 공인연비는 고른 차종에서 정해진다 */
data class Settings(
    val carModel: String?,
    val fuelEfficiencyKmPerLiter: Double?,
    val fuelPriceWonPerLiter: Int = AppConfig.DEFAULT_FUEL_PRICE_WON_PER_LITER,
    val homeDongCode: String?,
)
