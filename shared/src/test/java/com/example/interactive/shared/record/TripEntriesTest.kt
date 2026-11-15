package com.example.interactive.shared.record

import com.example.interactive.shared.model.RoutePoint
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.sample.SampleTrip
import com.example.interactive.shared.vehicle.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripEntriesTest {

    private fun trip(id: Long, start: Long, end: Long?, route: List<RoutePoint> = emptyList()) =
        Trip(id, start, end, 100.0, end?.let { 150.0 }, null, null, route)

    private fun stop(id: Long, tripId: Long, start: Long, lat: Double?, lon: Double? = 128.9) =
        Stop(id, tripId, lat, lon, "D$id", start, start + 1_000, 1)

    @Test
    fun mergesSampleAndRecordedNewestFirstWithoutKeyCollision() {
        val sample = listOf(SampleTrip(trip(1, 1_000, 2_000), emptyList()), SampleTrip(trip(2, 5_000, 6_000), emptyList()))
        val entries = TripEntries.build(sample, listOf(trip(1, 9_000, null)), emptyList())
        assertEquals(listOf("rec-1", "sample-2", "sample-1"), entries.map { it.key })
        assertTrue(entries.first().live)
        assertFalse(entries.last().live)
    }

    @Test
    fun recordedStopsAreGroupedByTrip() {
        val stops = listOf(stop(1, 1, 100, 37.7), stop(2, 2, 200, 37.8), stop(3, 1, 300, 37.9))
        val entries = TripEntries.build(emptyList(), listOf(trip(1, 0, 10), trip(2, 50, 60)), stops)
        assertEquals(listOf(2L), entries.first { it.key == "rec-2" }.stops.map { it.id })
        assertEquals(listOf(1L, 3L), entries.first { it.key == "rec-1" }.stops.map { it.id })
    }

    @Test
    fun pathUsesRoutePointsWhenThereAreEnoughElseStopsInOrder() {
        val route = listOf(RoutePoint(1, 37.1, 128.1), RoutePoint(2, 37.2, 128.2))
        val withRoute = TripEntry("a", trip(1, 0, 10, route), emptyList(), false)
        assertEquals(listOf(LatLng(37.1, 128.1), LatLng(37.2, 128.2)), withRoute.path())

        val noRoute = TripEntry("b", trip(2, 0, 10), listOf(stop(2, 2, 300, 37.9), stop(1, 2, 100, 37.7), stop(3, 2, 200, null)), false)
        assertEquals(listOf(LatLng(37.7, 128.9), LatLng(37.9, 128.9)), noRoute.path()) // 시간순, 위치 없는 정차는 뺀다
        assertEquals(noRoute.path(), noRoute.stopPoints())
    }
}
