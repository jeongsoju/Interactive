package com.example.interactive.shared.record

import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.record.TerritoryScorer.MY_ID

/** 내 땅 하나. rival은 나를 뺀 사람 중 가장 센 사람(없으면 null). atRisk면 빼앗길 수 있다 */
data class OwnedLand(
    val dongCode: String,
    val myScore: Int,
    val rivalId: String?,
    val rivalScore: Int,
    val atRisk: Boolean,
)

/** 친구 순위의 한 줄: 그 사람이 주인인 동의 개수 */
data class RankEntry(val personId: String, val lands: Int)

/** 영토 탭이 보여 줄 값을 만든다 (docs/spec.md 화면 목록: 내 영토, 친구 순위) */
object TerritoryOverview {

    /** 내가 주인인 동. 내 점수가 높은 것부터 */
    fun myLands(territories: List<Territory>): List<OwnedLand> =
        territories.filter { it.ownerId == MY_ID }.map { t ->
            val mine = t.scores[MY_ID] ?: 0
            val rival = t.scores.filterKeys { it != MY_ID }.maxByOrNull { it.value }
            val rivalScore = rival?.value ?: 0
            OwnedLand(
                dongCode = t.dongCode,
                myScore = mine,
                rivalId = rival?.key,
                rivalScore = rivalScore,
                atRisk = rivalScore > 0 && rivalScore >= mine * AppConfig.AT_RISK_SCORE_RATIO,
            )
        }.sortedByDescending { it.myScore }

    /** 나와 친구들을 땅이 많은 순으로. 땅이 하나도 없는 사람도 0곳으로 보여 준다. 같으면 나·친구 순서를 지킨다 */
    fun ranking(territories: List<Territory>, friendIds: List<String>): List<RankEntry> {
        val counts = territories.mapNotNull { it.ownerId }.groupingBy { it }.eachCount()
        return (listOf(MY_ID) + friendIds).distinct().map { RankEntry(it, counts[it] ?: 0) }.sortedByDescending { it.lands }
    }
}
