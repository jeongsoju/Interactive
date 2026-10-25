package com.example.interactive.shared.record

import com.example.interactive.shared.model.Territory
import com.example.interactive.shared.record.TerritoryScorer.MY_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerritoryOverviewTest {

    private val territories = listOf(
        Territory("A", MY_ID, mapOf(MY_ID to 100, "minji" to 85)), // 85 >= 80 → 위험
        Territory("B", MY_ID, mapOf(MY_ID to 300, "minji" to 90, "junho" to 50)), // 90 < 240 → 안전
        Territory("C", "minji", mapOf("minji" to 200, MY_ID to 40)),
        Territory("D", MY_ID, mapOf(MY_ID to 40)), // 경쟁자 없음 → 안전
        Territory("E", "junho", mapOf("junho" to 10)),
    )

    @Test
    fun myLandsAreOnlyMineSortedByMyScore() {
        assertEquals(listOf("B", "A", "D"), TerritoryOverview.myLands(territories).map { it.dongCode })
    }

    @Test
    fun atRiskWhenTheStrongestRivalIsCloseEnough() {
        val lands = TerritoryOverview.myLands(territories).associateBy { it.dongCode }
        assertTrue(lands.getValue("A").atRisk)
        assertEquals("minji", lands.getValue("A").rivalId)
        assertFalse(lands.getValue("B").atRisk)
        assertEquals(90, lands.getValue("B").rivalScore) // 경쟁자 중 가장 센 사람
        assertFalse(lands.getValue("D").atRisk)
        assertNull(lands.getValue("D").rivalId)
    }

    @Test
    fun exactlyAtTheRatioCountsAsRisk() {
        val land = TerritoryOverview.myLands(listOf(Territory("X", MY_ID, mapOf(MY_ID to 100, "minji" to 80)))).single()
        assertTrue(land.atRisk)
    }

    @Test
    fun rankingCountsLandsAndKeepsPeopleWithNone() {
        val ranking = TerritoryOverview.ranking(territories, listOf("minji", "junho", "sora"))
        assertEquals(listOf(MY_ID to 3, "minji" to 1, "junho" to 1, "sora" to 0), ranking.map { it.personId to it.lands })
    }

    @Test
    fun sameCountKeepsTheGivenOrder() {
        val t = listOf(Territory("A", "junho", mapOf("junho" to 5)), Territory("B", "minji", mapOf("minji" to 5)))
        // 땅이 같으면(1곳씩) 준 순서(민지, 준호)를 지키고, 0곳인 나는 맨 뒤
        assertEquals(listOf("minji", "junho", MY_ID), TerritoryOverview.ranking(t, listOf("minji", "junho")).map { it.personId })
    }
}
