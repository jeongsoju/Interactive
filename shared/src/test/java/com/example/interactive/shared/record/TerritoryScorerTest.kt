package com.example.interactive.shared.record

import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.record.TerritoryScorer.MY_ID
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TerritoryScorerTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour
    private val now = Instant.parse("2026-10-25T12:00:00Z").toEpochMilli()
    private var nextId = 1L

    private fun stop(dong: String?, startMillis: Long, stayMinutes: Long?) = Stop(
        id = nextId++, tripId = 1, latitude = 37.7, longitude = 128.9, dongCode = dong,
        startMillis = startMillis, endMillis = stayMinutes?.let { startMillis + it * minute }, occupants = 1,
    )

    private fun scores(vararg stops: Stop, home: String? = null) =
        TerritoryScorer.myScores(stops.toList(), now, home, ZoneOffset.UTC)

    @Test
    fun scoreIsStayMinutesPlusThirtyPerVisitDay() {
        // 같은 날 2번(60분+30분) → 90분 + 하루 30점, 다른 날 1번(45분) → 45분 + 30점
        val s = scores(
            stop("A", now - 2 * day + hour, 60), stop("A", now - 2 * day + 5 * hour, 30),
            stop("A", now - 5 * day, 45),
        )
        assertEquals(mapOf("A" to (90 + 45 + 2 * 30)), s)
    }

    @Test
    fun oneStayCountsAtMostFourHours() {
        assertEquals(240 + 30, scores(stop("A", now - day, 600))["A"])
    }

    @Test
    fun shortStopsLocationlessAndOldStaysDoNotCount() {
        val s = scores(
            stop("A", now - day, 9), // 10분 미만
            stop(null, now - day, 60), // 위치 없음
            stop("B", now - 31 * day, 60), // 30일 밖
        )
        assertEquals(emptyMap<String, Int>(), s)
    }

    @Test
    fun ongoingStayCountsOnceItReachesTenMinutes() {
        assertNull(scores(stop("A", now - 9 * minute, null))["A"])
        assertEquals(15 + 30, scores(stop("A", now - 15 * minute, null))["A"])
    }

    @Test
    fun homeDongIsLeftOut() {
        val s = scores(stop("HOME", now - day, 600), stop("A", now - day, 20), home = "HOME")
        assertEquals(mapOf("A" to 50), s)
    }

    @Test
    fun highestScoreOwnsTheDong() {
        val r = TerritoryScorer.compute(
            myScores = mapOf("A" to 100, "B" to 50),
            friendScores = mapOf("A" to mapOf("minji" to 480), "B" to mapOf("minji" to 10, "junho" to 20)),
            previousOwners = emptyMap(),
        )
        assertEquals("minji", r.territories.first { it.dongCode == "A" }.ownerId)
        assertEquals(MY_ID, r.territories.first { it.dongCode == "B" }.ownerId)
    }

    @Test
    fun tieKeepsTheCurrentOwner() {
        val friends = mapOf("A" to mapOf("minji" to 100))
        val mine = mapOf("A" to 100)
        assertEquals("minji", TerritoryScorer.compute(mine, friends, mapOf("A" to "minji")).territories.single().ownerId)
        assertEquals(MY_ID, TerritoryScorer.compute(mine, friends, mapOf("A" to MY_ID)).territories.single().ownerId)
    }

    @Test
    fun noScoreMeansNoOwner() {
        val r = TerritoryScorer.compute(emptyMap(), emptyMap(), mapOf("A" to MY_ID))
        assertNull(r.territories.single().ownerId)
        assertEquals(listOf(TerritoryChange("A", MY_ID, null)), r.changes)
    }

    @Test
    fun changesListOnlyDongsWhoseOwnerChanged() {
        val r = TerritoryScorer.compute(
            myScores = mapOf("A" to 300, "B" to 40),
            friendScores = mapOf("A" to mapOf("minji" to 200), "B" to mapOf("minji" to 90), "C" to mapOf("junho" to 5)),
            previousOwners = mapOf("A" to "minji", "B" to "minji", "C" to "junho"),
        )
        assertEquals(listOf(TerritoryChange("A", "minji", MY_ID)), r.changes)
    }

    @Test
    fun firstTimeVisitToUnownedDongClaimsIt() {
        val r = TerritoryScorer.compute(mapOf("D" to 40), emptyMap(), emptyMap())
        assertEquals(listOf(TerritoryChange("D", null, MY_ID)), r.changes)
    }
}
