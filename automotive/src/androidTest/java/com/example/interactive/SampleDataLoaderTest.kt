package com.example.interactive

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** assets/sample_data.json을 실제로 읽는다. org.json이 안드로이드 안에만 있어서 기기에서 돌린다 */
@RunWith(AndroidJUnit4::class)
class SampleDataLoaderTest {

    private val now = 1_700_000_000_000L
    private val data = SampleDataLoader.load(InstrumentationRegistry.getInstrumentation().targetContext, now)

    @Test
    fun readsEveryList() {
        assertEquals(6, data.dongs.size)
        assertEquals(3, data.pastTrips.size)
        assertEquals(listOf("민지", "준호"), data.friends.map { it.name })
        assertEquals(3, data.territories.size)
        assertEquals(7, data.replayTimeline.size)
    }

    @Test
    fun pastTripsAreInThePastAndLinkedToTheirStops() {
        data.pastTrips.forEach { sample ->
            assertTrue(sample.trip.startMillis < now)
            assertTrue(sample.stops.all { it.tripId == sample.trip.id && it.dongCode != null })
        }
    }

    @Test
    fun territoriesKeepFriendScores() {
        val first = data.territories.first()
        assertEquals("minji", first.ownerId)
        assertEquals(mapOf("minji" to 480, "junho" to 120), first.scores)
    }
}
