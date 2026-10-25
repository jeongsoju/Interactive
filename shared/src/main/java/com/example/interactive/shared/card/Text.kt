package com.example.interactive.shared.card

import kotlin.math.roundToInt

/** 한국어 조사 고르기. 마지막 글자에 받침이 있으면 첫 번째, 없으면 두 번째 */
object Korean {
    private fun hasFinalConsonant(word: String): Boolean {
        val last = word.lastOrNull() ?: return false
        return if (last in '가'..'힣') (last - '가') % 28 != 0 else false
    }

    fun eulReul(word: String) = word + if (hasFinalConsonant(word)) "을" else "를"
    fun iGa(word: String) = word + if (hasFinalConsonant(word)) "이" else "가"
}

/** 연료비 추정: 거리 ÷ 연비 × 유가 (docs/spec.md F1) */
fun estimateFuelCostWon(distanceKm: Double, kmPerLiter: Double, wonPerLiter: Int): Int =
    (distanceKm / kmPerLiter * wonPerLiter).roundToInt()

/** 1만 9천 원, 4만 2천 원, 8,500원 처럼 읽기 쉽게. 100원 단위로 반올림한다 */
fun formatWon(won: Int): String {
    val rounded = (won / 100.0).roundToInt() * 100
    val man = rounded / 10_000
    val cheon = rounded % 10_000 / 1_000
    return when {
        man > 0 && cheon > 0 -> "${man}만 ${cheon}천 원"
        man > 0 -> "${man}만 원"
        else -> "%,d원".format(rounded)
    }
}
