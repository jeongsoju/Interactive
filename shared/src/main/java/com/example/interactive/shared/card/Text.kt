package com.example.interactive.shared.card

import java.util.Locale

/** 한국어 조사 고르기. 마지막 글자에 받침이 있으면 첫 번째, 없으면 두 번째 */
object Korean {
    private fun hasFinalConsonant(word: String): Boolean {
        val last = word.lastOrNull() ?: return false
        return if (last in '가'..'힣') (last - '가') % 28 != 0 else false
    }

    fun eulReul(word: String) = word + if (hasFinalConsonant(word)) "을" else "를"
    fun iGa(word: String) = word + if (hasFinalConsonant(word)) "이" else "가"
}

/** 연료 사용량 추정(L) = 거리 ÷ 연비 (docs/spec.md F1) */
fun estimateFuelLiters(distanceKm: Double, kmPerLiter: Double): Double = distanceKm / kmPerLiter

/**
 * 여행의 에너지 사용 한 줄 (docs/spec.md F1). 전기차(배터리 % 있음)는 "배터리 5% 사용",
 * 내연기관은 "연료 약 3.2L (추정) · 연비 14.0km/L (공인)". 차가 주는 연비 값은 아직 읽지 않아 늘 공인연비다.
 */
fun energySummary(distanceKm: Double, batteryUsedPercent: Int?, kmPerLiter: Double): String =
    if (batteryUsedPercent != null) "배터리 ${batteryUsedPercent}% 사용"
    else "연료 약 ${formatLiters(estimateFuelLiters(distanceKm, kmPerLiter))} (추정) · 연비 ${formatKmPerLiter(kmPerLiter)} (공인)"

fun formatLiters(liters: Double) = "%.1fL".format(Locale.KOREA, liters)

fun formatKmPerLiter(kmPerLiter: Double) = "%.1fkm/L".format(Locale.KOREA, kmPerLiter)
