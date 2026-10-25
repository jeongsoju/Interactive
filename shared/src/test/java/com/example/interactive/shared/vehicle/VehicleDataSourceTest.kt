package com.example.interactive.shared.vehicle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleDataSourceTest {

    private val a = LatLng(37.0, 128.0)
    private val b = LatLng(38.0, 129.0)

    // 0분 P(a) → 10분 D(a) → 20분 P(b)
    private val timeline = listOf(
        ReplayPoint(0, Gear.P, a, 100.0, 2),
        ReplayPoint(600, Gear.D, a, 100.0, 2),
        ReplayPoint(1200, Gear.P, b, 110.0, 3),
    )

    @Test
    fun replayStartsAtFirstPointWithStartTime() {
        val replay = ReplayVehicleDataSource(timeline, startMillis = 1_000L)
        assertEquals(Gear.P, replay.state.gear)
        assertEquals(1_000L, replay.state.nowMillis)
    }

    @Test
    fun replayTimeFlowsFasterWithSpeed() {
        val replay = ReplayVehicleDataSource(timeline, startMillis = 0L, speed = 60)
        replay.tick(1_000L) // 실제 1초 = 가상 60초
        assertEquals(60_000L, replay.state.nowMillis)
    }

    @Test
    fun replayChangesGearAndInterpolatesWhileDriving() {
        val replay = ReplayVehicleDataSource(timeline, startMillis = 0L, speed = 1)
        replay.tick(15 * 60_000L) // 가상 15분: D, a→b 절반
        assertEquals(Gear.D, replay.state.gear)
        assertEquals(37.5, replay.state.location!!.latitude, 1e-9)
        assertEquals(105.0, replay.state.odometerKm, 1e-9)
    }

    @Test
    fun replayStaysAtLastPointButTimeKeepsFlowing() {
        val replay = ReplayVehicleDataSource(timeline, startMillis = 0L, speed = 1)
        replay.tick(30 * 60_000L)
        assertEquals(Gear.P, replay.state.gear)
        assertEquals(b, replay.state.location)
        assertEquals(3, replay.state.occupants)
        assertEquals(30 * 60_000L, replay.state.nowMillis)
    }

    @Test
    fun replayRestartGoesBackToStart() {
        val replay = ReplayVehicleDataSource(timeline, startMillis = 0L, speed = 1)
        replay.tick(15 * 60_000L)
        replay.restart()
        assertEquals(Gear.P, replay.state.gear)
        assertEquals(0L, replay.state.nowMillis)
    }

    @Test
    fun manualButtonsChangeState() {
        val manual = ManualVehicleDataSource(VehicleState(Gear.P, a, 100.0, 1, 0L))
        var notified = 0
        manual.setOnChange { notified++ }

        manual.setGear(Gear.D)
        manual.setLocation(b)
        manual.advanceTime(10 * 60_000L)
        manual.setOccupants(9)
        manual.addOdometer(10.0)

        assertEquals(Gear.D, manual.state.gear)
        assertEquals(b, manual.state.location)
        assertEquals(10 * 60_000L, manual.state.nowMillis)
        assertEquals(4, manual.state.occupants) // 1~4명으로 제한
        assertEquals(110.0, manual.state.odometerKm, 1e-9)
        assertEquals(5, notified)
    }

    @Test
    fun manualTimeDoesNotFlowByItself() {
        val manual = ManualVehicleDataSource(VehicleState(Gear.P, null, 0.0, 1, 5L))
        assertNull(manual.state.location)
        assertEquals(5L, manual.state.nowMillis)
    }
}
