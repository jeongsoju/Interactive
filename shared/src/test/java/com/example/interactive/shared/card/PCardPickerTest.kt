package com.example.interactive.shared.card

import com.example.interactive.shared.model.Dong
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.record.TerritoryChange
import com.example.interactive.shared.record.TerritoryScorer.MY_ID
import com.example.interactive.shared.vehicle.LatLng
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PCardPickerTest {

    private val minute = 60_000L
    private val day = 24 * 60 * minute
    private val now = Instant.parse("2026-10-25T12:00:00Z").toEpochMilli()
    private val dong = Dong("A", "초당동", LatLng(0.0, 0.0), emptyList())
    private val parked = Stop(9, 1, 0.0, 0.0, "A", now, null, 2)
    private val names = mapOf("minji" to "민지", "junho" to "준호")

    private fun past(dongCode: String, daysAgo: Int, minutes: Long) = Stop(
        id = daysAgo.toLong(), tripId = 1, latitude = 0.0, longitude = 0.0, dongCode = dongCode,
        startMillis = now - daysAgo * day, endMillis = now - daysAgo * day + minutes * minute, occupants = 1,
    )

    private fun input(
        previous: List<Stop> = emptyList(),
        territory: Territory? = null,
        dong: Dong? = this.dong,
    ) = PCardInput(parked, dong, previous, territory, 12.4, energySummary(12.4, null, 14.0), { names.getValue(it) }, ZoneOffset.UTC)

    private val territoryCardInput
        get() = input(listOf(past("A", 3, 20)), Territory("A", "minji", mapOf("minji" to 80, MY_ID to 50)))

    @Test
    fun firstTimeInUnownedDongShowsFirstVisitWithProgress() {
        val card = PCardPicker.pick(input())
        assertEquals(PCardKind.FIRST_VISIT, card.kind)
        assertTrue(card.showProgress)
        assertEquals("여기서 10분 넘게 머물면 초당동이 내 영토가 돼요.", card.body)
    }

    @Test
    fun friendLandThatTenMinutesCanTakeShowsTerritoryCard() {
        // 내 지난 점수 20분+30=50, 이번 10분+30을 더하면 90 > 민지 80
        val card = PCardPicker.pick(territoryCardInput)
        assertEquals(PCardKind.TERRITORY, card.kind)
        assertEquals("초당동을 되찾을 수 있어요", card.title)
        assertEquals("지금은 민지님 땅이에요. 10분 머물면 내 땅이 돼요.", card.body)
    }

    @Test
    fun friendTooFarAheadAndAlreadyVisitedFallsBackToSummary() {
        val card = PCardPicker.pick(input(listOf(past("A", 3, 20)), Territory("A", "minji", mapOf("minji" to 480))))
        assertEquals(PCardKind.TRIP_SUMMARY, card.kind)
    }

    @Test
    fun neverBeenToAFriendDongIsFirstVisitWhenItCannotBeTaken() {
        val card = PCardPicker.pick(input(territory = Territory("A", "junho", mapOf("junho" to 300))))
        assertEquals(PCardKind.FIRST_VISIT, card.kind)
        assertEquals("준호님 땅이에요. 자주 오고 오래 머물면 가져올 수 있어요.", card.body)
    }

    @Test
    fun visitedDongOrNoDongFallsBackToTripSummary() {
        val visited = PCardPicker.pick(input(listOf(past("A", 2, 30)), Territory("A", MY_ID, mapOf(MY_ID to 60))))
        assertEquals(PCardKind.TRIP_SUMMARY, visited.kind)
        assertEquals("지금까지 12km, 연료 약 0.9L (추정) · 연비 14.0km/L (공인)", visited.body)
        assertEquals(PCardKind.TRIP_SUMMARY, PCardPicker.pick(input(dong = null)).kind)
    }

    @Test
    fun territoryCardChangesOnceWhenTheDongBecomesMine() {
        val card = PCardPicker.pick(territoryCardInput)
        val change = TerritoryChange("A", "minji", MY_ID)
        val done = PCardPicker.confirmed(card, change, "초당동") { names.getValue(it) }!!
        assertEquals("초당동이 내 땅이 됐어요", done.title)
        assertEquals("민지님에게서 차지했어요.", done.body)
        assertTrue(done.confirmed)
        assertNull(PCardPicker.confirmed(done, change, "초당동") { names.getValue(it) }) // 두 번째는 없다
    }

    @Test
    fun otherCardsAndOtherDongsDoNotChange() {
        val first = PCardPicker.pick(input())
        assertNull(PCardPicker.confirmed(first, TerritoryChange("A", null, MY_ID), "초당동") { it })
        val territory = PCardPicker.pick(territoryCardInput)
        assertNull(PCardPicker.confirmed(territory, TerritoryChange("B", null, MY_ID), "다동") { it })
        assertFalse(territory.confirmed)
    }

    @Test
    fun josaFollowsTheFinalConsonant() {
        assertEquals("초당동을", Korean.eulReul("초당동"))
        assertEquals("경포대를", Korean.eulReul("경포대"))
        assertEquals("송정동이", Korean.iGa("송정동"))
        assertEquals("교1동이", Korean.iGa("교1동"))
    }

    @Test
    fun energySummaryShowsFuelOrBattery() {
        assertEquals(10.0, estimateFuelLiters(140.0, 14.0), 1e-9)
        assertEquals("연료 약 10.0L (추정) · 연비 14.0km/L (공인)", energySummary(140.0, null, 14.0))
        assertEquals("배터리 5% 사용", energySummary(140.0, 5, 14.0))
    }
}
