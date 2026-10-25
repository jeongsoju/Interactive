package com.example.interactive.shared.record

import com.example.interactive.shared.record.TripEvent.StayConfirmed
import com.example.interactive.shared.record.TripEvent.StopEnded
import com.example.interactive.shared.record.TripEvent.TripEnded
import com.example.interactive.shared.record.TripEvent.TripStarted
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.LatLng
import com.example.interactive.shared.vehicle.ReplayPoint
import com.example.interactive.shared.vehicle.ReplayVehicleDataSource
import com.example.interactive.shared.vehicle.VehicleState
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripRecorderTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val place = LatLng(37.75, 128.89)

    // 2026-10-25 10:00 UTC. 시간대는 UTC로 고정해 날짜 계산을 예측할 수 있게 한다
    private val t0 = Instant.parse("2026-10-25T10:00:00Z").toEpochMilli()
    private val recorder = TripRecorder(ZoneOffset.UTC)

    private fun feed(gear: Gear, atMillis: Long, odo: Double = 100.0, location: LatLng? = place) =
        recorder.onState(VehicleState(gear, location, odo, occupants = 2, nowMillis = atMillis))

    @Test
    fun parkedBeforeFirstDriveStartsNothing() {
        feed(Gear.P, t0)
        assertTrue(recorder.trips.isEmpty())
        assertTrue(recorder.stops.isEmpty())
    }

    @Test
    fun firstDStartsTrip() {
        feed(Gear.P, t0)
        val events = feed(Gear.D, t0 + minute, odo = 105.0)
        assertTrue(events.single() is TripStarted)
        assertEquals(105.0, recorder.trips.single().startOdometerKm, 1e-9)
    }

    @Test
    fun stopShorterThan10MinutesIsDiscarded() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + hour)
        val events = feed(Gear.D, t0 + hour + 9 * minute + 59_000L)
        assertEquals(false, (events.single() as StopEnded).kept)
        assertTrue(recorder.stops.isEmpty())
        assertEquals(1, recorder.discardedStops.size)
    }

    @Test
    fun stopOf10MinutesIsConfirmedOnceAndKept() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + hour, location = place)
        assertTrue(feed(Gear.P, t0 + hour + 9 * minute).none { it is StayConfirmed })
        assertEquals(1, feed(Gear.P, t0 + hour + 10 * minute).count { it is StayConfirmed })
        assertTrue(feed(Gear.P, t0 + hour + 11 * minute).none { it is StayConfirmed })

        val events = feed(Gear.D, t0 + hour + 25 * minute)
        assertEquals(true, (events.single() as StopEnded).kept)
        val stop = recorder.stops.single()
        assertEquals(25L, stop.stayedMinutes(t0 + 2 * hour))
        assertEquals(2, stop.occupants)
    }

    @Test
    fun reverseAlsoEndsTheStop() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + hour)
        val events = feed(Gear.R, t0 + hour + 15 * minute)
        assertTrue(events.last() is StopEnded)
        assertEquals(t0 + hour + 15 * minute, recorder.stops.single().endMillis)
    }

    @Test
    fun neutralIsIgnored() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + hour)
        assertTrue(feed(Gear.N, t0 + hour + 5 * minute).isEmpty())
        // N을 거쳐 D로 가도 정차는 D에서 끝난다
        val events = feed(Gear.D, t0 + hour + 20 * minute)
        assertEquals(true, (events.last() as StopEnded).kept)
    }

    @Test
    fun missingLocationIsKeptAsNoLocation() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + hour, location = null)
        val stop = recorder.stops.single()
        assertNull(stop.latitude)
        assertFalse(stop.hasLocation)
    }

    @Test
    fun tripEndsAfter3HoursParked() {
        feed(Gear.D, t0, odo = 100.0)
        feed(Gear.P, t0 + hour, odo = 140.0)
        assertTrue(feed(Gear.P, t0 + hour + 3 * hour - minute, odo = 140.0).none { it is TripEnded })

        val events = feed(Gear.P, t0 + hour + 3 * hour, odo = 140.0)
        val ended = events.filterIsInstance<TripEnded>().single().trip
        assertEquals(t0 + hour, ended.endMillis) // 끝 시각은 마지막 P가 된 때
        assertEquals(40.0, ended.distanceKm(), 1e-9)
        assertEquals(1, recorder.stops.size) // 체류 확정된 정차는 남는다
    }

    @Test
    fun tripEndsWhenDateChanges() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + 13 * hour + 50 * minute) // 23:50
        // 3시간이 안 돼도 자정을 넘기면 끝난다 (23:50 → 00:05)
        val events = feed(Gear.P, t0 + 14 * hour + 5 * minute)
        assertTrue(events.any { it is TripEnded })
    }

    @Test
    fun nextDStartsNewTripAfterTripEnded() {
        feed(Gear.D, t0)
        feed(Gear.P, t0 + hour)
        feed(Gear.P, t0 + 5 * hour)
        feed(Gear.D, t0 + 5 * hour + minute)
        assertEquals(listOf(1L, 2L), recorder.trips.map { it.id })
        assertNull(recorder.trips.last().endMillis)
    }

    @Test
    fun replayTripGivesTwoStaysAndOneDiscarded() {
        fun p(min: Long, gear: Gear, odo: Double) = ReplayPoint(min * 60, gear, place, odo, 2)
        val replay = ReplayVehicleDataSource(
            listOf(
                p(0, Gear.P, 0.0), p(2, Gear.D, 0.0), p(15, Gear.P, 12.0), p(20, Gear.D, 12.0),
                p(35, Gear.P, 30.0), p(60, Gear.D, 30.0), p(80, Gear.P, 45.0),
            ),
            startMillis = t0, speed = 60,
        )
        recorder.onState(replay.state)
        repeat(100) { // 실제 1초 = 가상 1분, 가상 100분까지
            replay.tick(1_000L)
            recorder.onState(replay.state)
        }
        assertEquals(1, recorder.trips.size)
        assertEquals(2, recorder.stops.size)
        assertEquals(1, recorder.discardedStops.size)
        // 마지막 정차는 아직 끝나지 않았지만 10분을 채워 체류로 확정됐다
        assertNull(recorder.stops.last().endMillis)
        assertTrue(recorder.stops.last().isStay(replay.state.nowMillis))
    }
}
