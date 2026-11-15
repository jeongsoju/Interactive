package com.example.interactive.shared.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {

    private val minute = 60_000L

    private fun stop(start: Long, end: Long?, dongCode: String? = "4215052000") =
        Stop(id = 1, tripId = 1, latitude = 37.75, longitude = 128.89, dongCode = dongCode,
            startMillis = start, endMillis = end, occupants = 2)

    @Test
    fun stayOf10MinutesCountsAsStay() {
        assertTrue(stop(0, 10 * minute).isStay(nowMillis = 99 * minute))
    }

    @Test
    fun stayOf9MinutesIsJustAPause() {
        assertFalse(stop(0, 9 * minute + 59_000).isStay(nowMillis = 99 * minute))
    }

    @Test
    fun ongoingStopUsesNow() {
        val ongoing = stop(0, null)
        assertFalse(ongoing.isStay(nowMillis = 5 * minute))
        assertTrue(ongoing.isStay(nowMillis = 10 * minute))
        assertEquals(12L, ongoing.stayedMinutes(nowMillis = 12 * minute))
    }

    @Test
    fun stopWithoutLocationIsExcluded() {
        assertFalse(stop(0, 20 * minute, dongCode = null).hasLocation)
    }

    @Test
    fun tripDistanceFromOdometer() {
        val trip = Trip(id = 1, startMillis = 0, endMillis = null, startOdometerKm = 1000.0,
            endOdometerKm = null, startBatteryPercent = 80, endBatteryPercent = null, route = emptyList())
        assertEquals(0.0, trip.distanceKm(), 0.0)
        assertEquals(142.5, trip.distanceKm(currentOdometerKm = 1142.5), 0.001)
        assertEquals(30.0, trip.copy(endOdometerKm = 1030.0).distanceKm(currentOdometerKm = 2000.0), 0.001)
    }

    @Test
    fun batteryUsedIsStartMinusEnd() {
        val trip = Trip(id = 1, startMillis = 0, endMillis = null, startOdometerKm = 0.0, endOdometerKm = null,
            startBatteryPercent = 80, endBatteryPercent = null, route = emptyList())
        assertEquals(5, trip.batteryUsedPercent(currentBatteryPercent = 75))
        assertEquals(8, trip.copy(endBatteryPercent = 72).batteryUsedPercent(currentBatteryPercent = 10))
        assertEquals(null, trip.copy(startBatteryPercent = null).batteryUsedPercent(75))
    }
}
