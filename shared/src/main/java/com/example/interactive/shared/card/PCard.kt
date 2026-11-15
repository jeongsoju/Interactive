package com.example.interactive.shared.card

import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.model.Dong
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.record.TerritoryChange
import com.example.interactive.shared.record.TerritoryScorer
import java.time.ZoneId
import java.util.Locale

enum class PCardKind(val chip: String) {
    TERRITORY("영토"),
    FIRST_VISIT("새로운 곳"),
    TRIP_SUMMARY("여행 요약"),
}

/** P 카드 한 장. 주행 중에는 화면에 올리지 않는다. showProgress가 참이면 머문 시간 막대를 그린다 */
data class PCard(
    val kind: PCardKind,
    val dongCode: String?,
    val title: String,
    val body: String,
    val showProgress: Boolean = false,
    /** 영토 변화 카드가 이미 '내 땅이 됐어요'로 바뀌었는지. 바뀌는 것은 한 번뿐이다 */
    val confirmed: Boolean = false,
)

/** 카드를 고르는 데 필요한 것. 지금 막 시작한 정차와 그때의 형편 */
data class PCardInput(
    val stop: Stop,
    val dong: Dong?,
    val myPreviousStops: List<Stop>,
    val territory: Territory?,
    val tripDistanceKm: Double,
    /** 이번 여행 에너지 사용 한 줄 (energySummary) */
    val tripEnergySummary: String,
    val friendName: (String) -> String,
    val zone: ZoneId = ZoneId.systemDefault(),
)

/** 카드 고르기 (docs/spec.md F3): 영토 변화 → 처음 온 곳 → 여행 요약 순으로 하나만 */
object PCardPicker {

    fun pick(input: PCardInput): PCard =
        territoryCard(input) ?: firstVisitCard(input) ?: summaryCard(input)

    // 친구 땅인데 여기서 10분 머물면 내 점수가 친구를 넘는 경우
    private fun territoryCard(input: PCardInput): PCard? {
        val dong = input.dong ?: return null
        val territory = input.territory ?: return null
        val owner = territory.ownerId?.takeIf { it != TerritoryScorer.MY_ID } ?: return null
        val ownerScore = territory.scores[owner] ?: 0
        val confirmAt = input.stop.startMillis + AppConfig.STAY_MIN_MINUTES * 60_000L
        val hypothetical = input.stop.copy(endMillis = confirmAt)
        val projected = TerritoryScorer.myScores(input.myPreviousStops + hypothetical, confirmAt, null, input.zone)[dong.code] ?: 0
        if (projected <= ownerScore) return null
        val triedBefore = input.myPreviousStops.any { it.dongCode == dong.code }
        val verb = if (triedBefore) "되찾을" else "차지할"
        return PCard(
            kind = PCardKind.TERRITORY,
            dongCode = dong.code,
            title = "${Korean.eulReul(dong.name)} $verb 수 있어요",
            body = "지금은 ${input.friendName(owner)}님 땅이에요. ${AppConfig.STAY_MIN_MINUTES}분 머물면 내 땅이 돼요.",
        )
    }

    private fun firstVisitCard(input: PCardInput): PCard? {
        val dong = input.dong ?: return null
        if (input.myPreviousStops.any { it.dongCode == dong.code }) return null
        val owner = input.territory?.ownerId
        val body = when (owner) {
            null -> "여기서 ${AppConfig.STAY_MIN_MINUTES}분 넘게 머물면 ${Korean.iGa(dong.name)} 내 영토가 돼요."
            TerritoryScorer.MY_ID -> "${dong.name}은 이미 내 영토예요."
            else -> "${input.friendName(owner)}님 땅이에요. 자주 오고 오래 머물면 가져올 수 있어요."
        }
        return PCard(PCardKind.FIRST_VISIT, dong.code, "처음 와본 곳이에요", body, showProgress = true)
    }

    private fun summaryCard(input: PCardInput) = PCard(
        kind = PCardKind.TRIP_SUMMARY,
        dongCode = input.dong?.code,
        title = "이번 여행",
        body = "지금까지 ${"%.0f".format(Locale.KOREA, input.tripDistanceKm)}km, ${input.tripEnergySummary}",
    )

    /**
     * 영토 변화 카드가 떠 있는 동안 체류가 확정되어 정말 내 땅이 됐다면, 내용을 한 번만 바꾼다.
     * 바꿀 일이 아니면 null.
     */
    fun confirmed(card: PCard, change: TerritoryChange, dongName: String, friendName: (String) -> String): PCard? {
        if (card.kind != PCardKind.TERRITORY || card.confirmed) return null
        if (change.dongCode != card.dongCode || change.newOwnerId != TerritoryScorer.MY_ID) return null
        val from = change.previousOwnerId?.let { "${friendName(it)}님에게서 " } ?: ""
        return card.copy(
            title = "${Korean.iGa(dongName)} 내 땅이 됐어요",
            body = "${from}차지했어요.",
            showProgress = false,
            confirmed = true,
        )
    }
}
