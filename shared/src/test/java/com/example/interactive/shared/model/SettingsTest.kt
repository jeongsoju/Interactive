package com.example.interactive.shared.model

import com.example.interactive.shared.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {

    @Test
    fun withoutACarTheDefaultEfficiencyIsUsed() {
        assertEquals(AppConfig.DEFAULT_FUEL_EFFICIENCY_KM_PER_LITER, Settings.DEFAULT.efficiency, 1e-9)
    }

    @Test
    fun pickingACarSetsItsEfficiency() {
        val s = Settings.DEFAULT.withCar(CarModels.examples.first { it.name == "그랜저" })
        assertEquals("그랜저", s.carModel)
        assertEquals(11.0, s.efficiency, 1e-9)
    }

    @Test
    fun priceChangesInStepsAndStaysInRange() {
        val s = Settings.DEFAULT
        assertEquals(1_900, s.withPriceChange(50).fuelPriceWonPerLiter)
        assertEquals(Settings.MIN_PRICE, s.withPriceChange(-100_000).fuelPriceWonPerLiter)
        assertEquals(Settings.MAX_PRICE, s.withPriceChange(100_000).fuelPriceWonPerLiter)
    }

    @Test
    fun exampleCarsHaveDistinctNamesAndSaneEfficiency() {
        assertEquals(CarModels.examples.size, CarModels.examples.map { it.name }.toSet().size)
        assertTrue(CarModels.examples.all { it.kmPerLiter in 5.0..30.0 })
    }
}
