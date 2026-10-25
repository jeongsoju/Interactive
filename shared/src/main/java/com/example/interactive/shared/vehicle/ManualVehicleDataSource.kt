package com.example.interactive.shared.vehicle

/** 직접 조작 모드: 화면 버튼으로 기어·위치·시각·탑승 인원·주행거리를 바꾼다. 시간은 버튼으로만 넘어간다 */
class ManualVehicleDataSource(initial: VehicleState) : VehicleDataSource {

    private var listener: ((VehicleState) -> Unit)? = null

    override var state: VehicleState = initial
        private set

    override fun setOnChange(listener: ((VehicleState) -> Unit)?) {
        this.listener = listener
    }

    fun setGear(gear: Gear) = update { it.copy(gear = gear) }

    fun setLocation(location: LatLng) = update { it.copy(location = location) }

    fun advanceTime(millis: Long) = update { it.copy(nowMillis = it.nowMillis + millis) }

    fun setOccupants(count: Int) = update { it.copy(occupants = count.coerceIn(1, 4)) }

    fun addOdometer(km: Double) = update { it.copy(odometerKm = it.odometerKm + km) }

    fun reset(initial: VehicleState) = update { initial }

    private fun update(change: (VehicleState) -> VehicleState) {
        state = change(state)
        listener?.invoke(state)
    }
}
