package com.example.interactive

import android.Manifest
import android.annotation.SuppressLint
import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import ai.pleos.playground.vehicle.listener.SeatOccupancyListener
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.LatLng
import com.example.interactive.shared.vehicle.VehicleDataSource
import com.example.interactive.shared.vehicle.VehicleState

/**
 * 진짜 차(에뮬레이터는 Virtual Console)에서 읽는 통로 (docs/spec.md F1).
 * - 기어: AAOS CarPropertyManager GEAR_SELECTION (Pleos Gear API는 문서에 없어 쓰지 않는다)
 * - 누적거리·좌석 착석: Pleos Vehicle SDK (Odometer, Seat). 호출은 비동기라 결과가 오면 상태를 갱신한다
 * - 위치: 안드로이드 표준 LocationManager (권한이 있을 때만)
 * - 주행 규제: CarUxRestrictionsManager
 * 값을 아직 못 받은 항목은 기본값(P, 위치 없음, 0km, 0명)으로 둔다.
 */
class RealVehicleDataSource(private val context: Context) : VehicleDataSource {

    private val handler = Handler(Looper.getMainLooper())
    private var listener: ((VehicleState) -> Unit)? = null
    private var closed = false

    override var state = VehicleState(Gear.P, null, 0.0, 0, System.currentTimeMillis())
        private set

    /** 연결 상태·오류를 화면에 보여 주기 위한 메모 (개발 확인용) */
    val notes = mutableStateListOf<String>()

    /** 주행 규제가 켜져 있는지. 10단계에서 카드 숨기기에 쓴다 */
    var uxRestricted by mutableStateOf(false)
        private set

    private var car: Car? = null
    private var vehicle: Vehicle? = null
    private var locationManager: LocationManager? = null
    private val seatOccupied = mutableMapOf<SeatArea, Boolean>()
    private var pendingCallbacks = 0

    private val gearCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            (value.value as? Int)?.let { update { copy(gear = gearOf(it)) } }
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) = note("기어 오류 $propertyId")
    }

    private val locationListener = LocationListener { loc -> setLocation(loc) }

    private val distanceListener = object : DistanceDrivenListener {
        override fun onFailed(e: Exception) = note("누적거리 알림 오류: ${e.message}")

        override fun onDistanceDrivenUpdated(distance: Float?) {
            distance?.let { update { copy(odometerKm = it.toDouble()) } }
        }
    }

    // 기어가 안 바뀌어도 시간 규칙(체류 10분 등)이 돌도록 일정 간격으로 시각을 갱신한다
    private val clock = object : Runnable {
        override fun run() {
            update { this }
            handler.postDelayed(this, CLOCK_MILLIS)
        }
    }

    init {
        connectCar()
        connectVehicleSdk()
        startLocation()
        handler.postDelayed(clock, CLOCK_MILLIS)
    }

    override fun setOnChange(listener: ((VehicleState) -> Unit)?) {
        this.listener = listener
    }

    /** 위치 권한을 받은 뒤 다시 불러도 된다 */
    fun startLocation() {
        if (locationManager != null) return
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            note("위치 권한 없음")
            return
        }
        val manager = context.getSystemService(LocationManager::class.java)
        if (LocationManager.GPS_PROVIDER !in manager.allProviders) {
            note("GPS 장치 없음")
            return
        }
        locationManager = manager
        @SuppressLint("MissingPermission")
        fun begin() {
            manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let(::setLocation)
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_MILLIS, 0f, locationListener, Looper.getMainLooper())
        }
        begin()
        notes.remove("위치 권한 없음")
        note("위치 연결됨")
    }

    override fun close() {
        closed = true
        handler.removeCallbacks(clock)
        listener = null
        locationManager?.removeUpdates(locationListener)
        car?.disconnect()
        // 대기 중인 응답이 없을 때만 release (SDK 규칙)
        releaseVehicleIfIdle()
    }

    // ---- 기어 · 주행 규제 (AAOS car 서비스) ----

    private fun connectCar() {
        car = Car.createCar(context, handler, CAR_WAIT_MILLIS) { connected, ready ->
            if (!ready) {
                note("car 서비스 연결 끊김")
                return@createCar
            }
            val properties = connected.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            if (properties == null) {
                note("기어: CarPropertyManager 없음")
            } else {
                runCatching {
                    update { copy(gear = gearOf(properties.getIntProperty(VehiclePropertyIds.GEAR_SELECTION, 0))) }
                    properties.registerCallback(gearCallback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
                    note("기어 연결됨")
                }.onFailure { note("기어 읽기 실패: ${it.message}") }
            }
            val ux = connected.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
            if (ux != null) {
                uxRestricted = ux.currentCarUxRestrictions.isRequiresDistractionOptimization
                ux.registerListener { r: CarUxRestrictions -> uxRestricted = r.isRequiresDistractionOptimization }
                note("주행 규제 연결됨")
            }
        }
    }

    private fun gearOf(value: Int) = when (value) {
        VehicleGear.GEAR_PARK -> Gear.P
        VehicleGear.GEAR_REVERSE -> Gear.R
        VehicleGear.GEAR_NEUTRAL -> Gear.N
        else -> Gear.D // 드라이브와 저단 기어
    }

    // ---- 누적거리 · 좌석 (Pleos Vehicle SDK) ----

    private fun connectVehicleSdk() {
        val sdk = Vehicle(context)
        vehicle = sdk
        sdk.initialize()
        val odometer = sdk.getOdometer()
        pendingCallbacks++
        odometer.checkCurrentDistanceDrivenCapabilities(
            { _ ->
                odometer.getCurrentDistanceDriven(
                    { km -> done(); km?.let { v -> update { copy(odometerKm = v.toDouble()) } } },
                    { e -> done(); note("누적거리 읽기 실패: ${e.message}") },
                )
                odometer.registerDistanceDriven(distanceListener)
                note("누적거리 연결됨")
            },
            { e -> done(); note("누적거리 미지원: ${e.message}") },
        )
        val seat = sdk.getSeat()
        pendingCallbacks++
        seat.checkSeatOccupancyCapabilities(
            { areas: List<SeatArea> ->
                done()
                areas.forEach { area -> watchSeat(seat, area) }
                note("좌석 ${areas.size}곳 연결됨")
            },
            { e: Exception -> done(); note("좌석 미지원: ${e.message}") },
        )
    }

    private fun watchSeat(seat: ai.pleos.playground.vehicle.api.Seat, area: SeatArea) {
        pendingCallbacks++
        seat.getCurrentSeatOccupancy(
            area,
            { occupied -> done(); setSeat(area, occupied == true) },
            { e -> done(); note("좌석 읽기 실패: ${e.message}") },
        )
        seat.registerSeatOccupancy(area, object : SeatOccupancyListener {
            override fun onFailed(e: Exception) = note("좌석 알림 오류: ${e.message}")

            override fun onSeatOccupancyUpdated(occupied: Boolean?) = setSeat(area, occupied == true)
        })
    }

    private fun setSeat(area: SeatArea, occupied: Boolean) {
        seatOccupied[area] = occupied
        update { copy(occupants = seatOccupied.values.count { it }) }
    }

    private fun done() {
        pendingCallbacks--
        if (closed) releaseVehicleIfIdle()
    }

    private fun releaseVehicleIfIdle() {
        if (pendingCallbacks <= 0) {
            vehicle?.release()
            vehicle = null
        }
    }

    // ---- 위치 · 시계 · 상태 갱신 ----

    private fun setLocation(loc: Location) = update { copy(location = LatLng(loc.latitude, loc.longitude)) }

    private fun update(change: VehicleState.() -> VehicleState) {
        if (closed) return
        state = state.change().copy(nowMillis = System.currentTimeMillis())
        listener?.invoke(state)
    }

    private fun note(text: String) {
        if (text !in notes) notes += text
    }

    private companion object {
        const val CAR_WAIT_MILLIS = 5_000L
        const val CLOCK_MILLIS = 5_000L
        const val LOCATION_INTERVAL_MILLIS = 5_000L
    }
}
