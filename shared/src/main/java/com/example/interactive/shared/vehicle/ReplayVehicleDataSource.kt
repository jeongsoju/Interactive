package com.example.interactive.shared.vehicle

/** 가짜 여행의 한 지점. tSec은 재생을 시작한 뒤 흐른 (가상) 초 */
data class ReplayPoint(
    val tSec: Long,
    val gear: Gear,
    val location: LatLng,
    val odometerKm: Double,
    val occupants: Int,
    /** 전기차 배터리 %. 없으면 null */
    val batteryPercent: Int? = null,
)

/** 시연 모드: 가짜 여행을 시간순으로 재생한다. 앱이 tick()을 주기적으로 불러 시간을 흘린다 */
class ReplayVehicleDataSource(
    private val timeline: List<ReplayPoint>,
    private val startMillis: Long,
    speed: Int = 1,
) : VehicleDataSource {

    init {
        require(timeline.isNotEmpty()) { "재생할 여행이 비어 있다" }
    }

    /** 재생 속도 배수 (1, 10, 60) */
    var speed: Int = speed

    private var elapsedMillis = 0L
    private var listener: ((VehicleState) -> Unit)? = null

    override var state: VehicleState = stateAt(0L)
        private set

    override fun setOnChange(listener: ((VehicleState) -> Unit)?) {
        this.listener = listener
    }

    /** 실제로 realDeltaMillis만큼 지났다. 재생 속도만큼 가상 시간이 흐른다 */
    fun tick(realDeltaMillis: Long) {
        elapsedMillis += realDeltaMillis * speed
        state = stateAt(elapsedMillis)
        listener?.invoke(state)
    }

    fun restart() {
        elapsedMillis = 0L
        state = stateAt(0L)
        listener?.invoke(state)
    }

    // 기어·탑승 인원은 마지막으로 지난 지점의 값, 위치·주행거리는 다음 지점까지 직선으로 이어 간다
    private fun stateAt(elapsed: Long): VehicleState {
        val now = startMillis + elapsed
        val index = timeline.indexOfLast { it.tSec * 1000 <= elapsed }.coerceAtLeast(0)
        val cur = timeline[index]
        val next = timeline.getOrNull(index + 1)
        if (next == null || elapsed <= cur.tSec * 1000) {
            return VehicleState(cur.gear, cur.location, cur.odometerKm, cur.occupants, now, cur.batteryPercent)
        }
        val ratio = (elapsed - cur.tSec * 1000).toDouble() / ((next.tSec - cur.tSec) * 1000)
        fun mix(a: Double, b: Double) = a + (b - a) * ratio
        return VehicleState(
            gear = cur.gear,
            location = LatLng(
                mix(cur.location.latitude, next.location.latitude),
                mix(cur.location.longitude, next.location.longitude),
            ),
            odometerKm = mix(cur.odometerKm, next.odometerKm),
            occupants = cur.occupants,
            nowMillis = now,
            batteryPercent = if (cur.batteryPercent != null && next.batteryPercent != null)
                Math.round(mix(cur.batteryPercent.toDouble(), next.batteryPercent.toDouble())).toInt()
            else cur.batteryPercent,
        )
    }
}
