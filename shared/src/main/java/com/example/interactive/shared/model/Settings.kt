package com.example.interactive.shared.model

import com.example.interactive.shared.AppConfig

/** 차종 하나와 공인연비(km/L, 복합) */
data class CarModel(val name: String, val kmPerLiter: Double)

/**
 * 설정에서 고를 수 있는 차종. **예시 값이다**: 공식 자료로 확인한 연비가 아니므로
 * 제출 전에 한국에너지공단 등 공식 자료로 바꾼다 (docs/progress.md 13단계 메모).
 */
object CarModels {
    val examples = listOf(
        CarModel("아반떼", 14.0),
        CarModel("쏘나타", 12.5),
        CarModel("그랜저", 11.0),
        CarModel("투싼", 11.5),
        CarModel("싼타페", 10.5),
    )
}

/** 사용자 설정. 공인연비는 고른 차종에서 정해진다 */
data class Settings(
    val carModel: String?,
    val fuelEfficiencyKmPerLiter: Double?,
    val homeDongCode: String?,
) {
    /** 연료 사용량 계산에 쓰는 연비(공인). 차종을 안 골랐으면 기본값 */
    val efficiency: Double get() = fuelEfficiencyKmPerLiter ?: AppConfig.DEFAULT_FUEL_EFFICIENCY_KM_PER_LITER

    fun withCar(car: CarModel) = copy(carModel = car.name, fuelEfficiencyKmPerLiter = car.kmPerLiter)

    companion object {
        val DEFAULT = Settings(carModel = null, fuelEfficiencyKmPerLiter = null, homeDongCode = null)
    }
}
