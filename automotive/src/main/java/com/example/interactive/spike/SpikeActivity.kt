package com.example.interactive.spike

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.constant.area.SeatValue
import android.app.Activity
import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView

// 사전 조사용 실험 화면 (spike 브랜치 전용). 창 크기와 차량 값 읽기 결과를 화면과 logcat(SPIKE)에 출력한다.
open class SpikeActivity : Activity() {

    private lateinit var output: TextView
    private lateinit var vehicle: Vehicle
    private lateinit var car: Car
    private var propertyManager: CarPropertyManager? = null
    private var uxManager: CarUxRestrictionsManager? = null

    private val gearCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            show("AAOS_GEAR", gearName(value.value as Int))
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            show("AAOS_GEAR", "ERROR property=$propertyId area=$areaId")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        output = TextView(this).apply { textSize = 24f; setPadding(32, 32, 32, 32) }
        setContentView(ScrollView(this).apply { addView(output) })

        vehicle = Vehicle(this)
        vehicle.initialize()
        readVehicle()
        readLocation()
        readAaos()
    }

    // 기어는 AAOS 표준 GEAR_SELECTION, 정차/주행 판단은 CarUxRestrictions로 읽는다.
    private fun readAaos() {
        car = Car.createCar(this)
        val pm = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
        propertyManager = pm
        try {
            show("AAOS_GEAR_CONFIG", if (pm.getCarPropertyConfig(VehiclePropertyIds.GEAR_SELECTION) == null) "null" else "supported")
            pm.registerCallback(gearCallback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        } catch (e: Exception) {
            fail("AAOS_GEAR", e)
        }

        val ux = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager
        uxManager = ux
        showUx(ux.currentCarUxRestrictions)
        ux.registerListener { showUx(it) }
    }

    private fun showUx(r: CarUxRestrictions) {
        show("UX", "requiresDO=${r.isRequiresDistractionOptimization} active=0x${Integer.toHexString(r.activeRestrictions)}")
    }

    private fun gearName(gear: Int) = when (gear) {
        VehicleGear.GEAR_PARK -> "P"
        VehicleGear.GEAR_REVERSE -> "R"
        VehicleGear.GEAR_NEUTRAL -> "N"
        VehicleGear.GEAR_DRIVE -> "D"
        else -> "OTHER($gear)"
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) logWindowSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        logWindowSize()
    }

    private fun logWindowSize() {
        val bounds = windowManager.currentWindowMetrics.bounds
        val c = resources.configuration
        val density = resources.displayMetrics.densityDpi
        show("WINDOW", "${javaClass.simpleName} px=${bounds.width()}x${bounds.height()} " +
            "dp=${c.screenWidthDp}x${c.screenHeightDp} density=$density orientation=" +
            if (c.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait")
    }

    private fun readVehicle() {
        vehicle.getGear().getCurrentGearSelectionState({ show("GEAR", "$it") }, { fail("GEAR", it) })

        val seat = vehicle.getSeat()
        listOf(SeatValue.ROW_1_LEFT, SeatValue.ROW_1_RIGHT, SeatValue.ROW_2_LEFT, SeatValue.ROW_2_RIGHT).forEach { v ->
            seat.getCurrentSeatOccupancy(SeatArea(v), { show("SEAT_$v", "$it") }, { fail("SEAT_$v", it) })
        }

        val fuel = vehicle.getFuel()
        fuel.getCurrentFuelEconomy({ show("FUEL_ECONOMY_CURRENT", "$it") }, { fail("FUEL_ECONOMY_CURRENT", it) })
        fuel.getCumulativeFuelEconomy({ show("FUEL_ECONOMY_CUMULATIVE", "$it") }, { fail("FUEL_ECONOMY_CUMULATIVE", it) })
        fuel.getFuelPercent({ show("FUEL_PERCENT", "$it") }, { fail("FUEL_PERCENT", it) })

        val ev = vehicle.getEvBattery()
        ev.getCurrentEvEfficiency({ show("EV_EFFICIENCY_CURRENT", "$it") }, { fail("EV_EFFICIENCY_CURRENT", it) })
        ev.getEvBatteryStateOfCharge({ show("EV_SOC", "$it") }, { fail("EV_SOC", it) })

        val odo = vehicle.getOdometer()
        odo.getCurrentDistanceDriven({ show("ODOMETER", "$it") }, { fail("ODOMETER", it) })
        odo.getTripCurrentDrivingDistance({ show("TRIP_DISTANCE", "$it") }, { fail("TRIP_DISTANCE", it) })
        odo.getRangeRemainingDistance({ show("RANGE_REMAINING", "$it") }, { fail("RANGE_REMAINING", it) })
    }

    private fun readLocation() {
        val lm = getSystemService(LocationManager::class.java)
        try {
            show("LOCATION_PROVIDERS", "${lm.allProviders}")
            lm.allProviders.forEach { p ->
                val loc = lm.getLastKnownLocation(p)
                show("LOCATION_$p", if (loc == null) "null" else "${loc.latitude},${loc.longitude} t=${loc.time}")
            }
        } catch (e: SecurityException) {
            fail("LOCATION", e)
        }
    }

    private fun show(key: String, value: String) {
        Log.i(TAG, "$key = $value")
        runOnUiThread { output.append("$key = $value\n") }
    }

    private fun fail(key: String, e: Exception) = show(key, "FAILED ${e.javaClass.simpleName}: ${e.message}")

    override fun onDestroy() {
        // 실험용: 화면을 닫을 때 해제. 실제 앱에서는 콜백이 모두 끝난 뒤 release() 해야 한다.
        vehicle.release()
        propertyManager?.unregisterCallback(gearCallback)
        uxManager?.unregisterListener()
        car.disconnect()
        super.onDestroy()
    }

    companion object {
        const val TAG = "SPIKE"
    }
}

// screenOrientation="landscape"로 선언한 같은 화면 (창 2개 영역 확인용)
class SpikeLandActivity : SpikeActivity()
