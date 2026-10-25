package com.example.interactive.shared.record

import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Territory
import java.time.Instant
import java.time.ZoneId

/** 영토 주인이 바뀐 일. P 카드(10단계)가 "땅을 차지했어요/빼앗겼어요"를 띄울 때 쓴다 */
data class TerritoryChange(val dongCode: String, val previousOwnerId: String?, val newOwnerId: String?)

data class TerritoryResult(val territories: List<Territory>, val changes: List<TerritoryChange>)

/**
 * 영토 점수와 주인을 계산한다 (docs/spec.md F2). 거리·속도는 점수에 넣지 않는다.
 * 점수 = 최근 30일 체류 분(1회 최대 4시간) + 방문한 날 수 × 30. 우리 집 동네는 내 점수에서 뺀다.
 */
object TerritoryScorer {

    /** 내 사람 id. 친구는 샘플 데이터의 id(minji, junho)를 쓴다 */
    const val MY_ID = "me"

    /** 동마다 내 점수. 체류로 인정되는 정차(10분 이상, 아직 진행 중이면 now까지)만 센다 */
    fun myScores(
        stops: List<Stop>,
        nowMillis: Long,
        homeDongCode: String?,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Map<String, Int> {
        val windowStart = nowMillis - AppConfig.SCORE_WINDOW_DAYS * DAY_MILLIS
        val counted = stops.filter { stop ->
            stop.dongCode != null && stop.dongCode != homeDongCode &&
                stop.startMillis > windowStart && stop.startMillis <= nowMillis && stop.isStay(nowMillis)
        }
        return counted.groupBy { it.dongCode!! }.mapValues { (_, inDong) ->
            val minutes = inDong.sumOf { minOf(it.stayedMinutes(nowMillis), AppConfig.MAX_STAY_MINUTES_PER_VISIT.toLong()) }
            val days = inDong.map { Instant.ofEpochMilli(it.startMillis).atZone(zone).toLocalDate() }.toSet().size
            (minutes + days * AppConfig.VISIT_DAY_BONUS).toInt()
        }
    }

    /**
     * 내 점수와 친구 점수를 합쳐 동마다 주인을 정한다.
     * 동점이면 지금 주인이 유지되고, 점수가 모두 0이면 주인이 없다.
     */
    fun compute(
        myScores: Map<String, Int>,
        friendScores: Map<String, Map<String, Int>>,
        previousOwners: Map<String, String?>,
    ): TerritoryResult {
        val territories = (myScores.keys + friendScores.keys + previousOwners.keys).sorted().map { dong ->
            val scores = (friendScores[dong].orEmpty() + (MY_ID to (myScores[dong] ?: 0))).filterValues { it > 0 }
            Territory(dong, ownerOf(scores, previousOwners[dong]), scores)
        }
        val changes = territories.mapNotNull { t ->
            val before = previousOwners[t.dongCode]
            if (t.ownerId != before) TerritoryChange(t.dongCode, before, t.ownerId) else null
        }
        return TerritoryResult(territories, changes)
    }

    private fun ownerOf(scores: Map<String, Int>, currentOwner: String?): String? {
        val best = scores.values.maxOrNull() ?: return null
        if (currentOwner != null && scores[currentOwner] == best) return currentOwner
        return scores.entries.filter { it.value == best }.minOf { it.key } // 처음 차지하는 동점은 id 순
    }

    private const val DAY_MILLIS = 24 * 60 * 60_000L
}
