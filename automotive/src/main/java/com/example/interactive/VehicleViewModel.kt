package com.example.interactive

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.model.Trip
import com.example.interactive.shared.record.TripRecorder
import com.example.interactive.shared.sample.SampleData
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.ManualVehicleDataSource
import com.example.interactive.shared.vehicle.ReplayVehicleDataSource
import com.example.interactive.shared.vehicle.VehicleDataSource
import com.example.interactive.shared.vehicle.VehicleState

enum class AppMode { REPLAY, MANUAL }

/** 지금까지 기록된 여행·정차. 임시 화면이 그대로 보여준다 */
data class RecordSnapshot(
    val trips: List<Trip> = emptyList(),
    val stops: List<Stop> = emptyList(),
    val discarded: List<Stop> = emptyList(),
)

private const val TICK_MILLIS = 250L
private const val PREFS = "interactive"
private const val KEY_MODE = "mode"

/** 고른 모드, 샘플 데이터, 차량 데이터 통로를 들고 있는다. 화면이 돌아가도 유지된다 */
class VehicleViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    var mode: AppMode? by mutableStateOf(
        prefs.getString(KEY_MODE, null)?.let { runCatching { AppMode.valueOf(it) }.getOrNull() }
    )
        private set

    /** 모드 선택창을 보여줄지. 저장된 모드가 없으면 처음부터 보인다 */
    var choosing by mutableStateOf(mode == null)
        private set

    var sample: SampleData by mutableStateOf(loadSample())
        private set

    var state: VehicleState? by mutableStateOf(null)
        private set

    var source: VehicleDataSource? = null
        private set

    var replaySpeed by mutableStateOf(1)
        private set

    var records by mutableStateOf(RecordSnapshot())
        private set

    private var recorder = TripRecorder()

    private var lastTickRealtime = 0L

    private val ticker = object : Runnable {
        override fun run() {
            val replay = source as? ReplayVehicleDataSource ?: return
            val nowReal = SystemClock.elapsedRealtime()
            replay.tick(nowReal - lastTickRealtime)
            lastTickRealtime = nowReal
            handler.postDelayed(this, TICK_MILLIS)
        }
    }

    init {
        buildSource()
    }

    fun showChooser() { choosing = true }

    fun hideChooser() { choosing = false }

    /** 모드를 고른다. 처음 고르는 것이 아니고 다른 모드라면 resetSample로 샘플 데이터 초기화 여부를 받는다 */
    fun chooseMode(newMode: AppMode, resetSample: Boolean) {
        mode = newMode
        prefs.edit().putString(KEY_MODE, newMode.name).apply()
        choosing = false
        if (resetSample) sample = loadSample()
        buildSource()
    }

    /** 샘플 데이터를 다시 읽고 지금 모드의 처음 상태로 돌아간다 */
    fun resetData() {
        sample = loadSample()
        buildSource()
    }

    fun changeReplaySpeed(speed: Int) {
        replaySpeed = speed
        (source as? ReplayVehicleDataSource)?.speed = speed
    }

    fun restartReplay() = (source as? ReplayVehicleDataSource)?.restart()

    fun manual(): ManualVehicleDataSource? = source as? ManualVehicleDataSource

    // 기기 시계는 여기서 시작 시각을 정할 때만 쓴다. 이후 앱의 시간은 모두 VehicleState.nowMillis
    private fun loadSample(): SampleData =
        SampleDataLoader.load(getApplication(), System.currentTimeMillis())

    private fun buildSource() {
        handler.removeCallbacks(ticker)
        source?.setOnChange(null)
        val startMillis = System.currentTimeMillis()
        val newSource: VehicleDataSource? = when (mode) {
            AppMode.REPLAY -> ReplayVehicleDataSource(sample.replayTimeline, startMillis, replaySpeed)
            AppMode.MANUAL -> ManualVehicleDataSource(
                VehicleState(Gear.P, sample.dongs.first().center, 12000.0, 1, startMillis)
            )
            null -> null
        }
        source = newSource
        state = newSource?.state
        recorder = TripRecorder()
        records = RecordSnapshot()
        newSource?.let { feedRecorder(it.state) }
        newSource?.setOnChange { feedRecorder(it) }
        if (newSource is ReplayVehicleDataSource) {
            lastTickRealtime = SystemClock.elapsedRealtime()
            handler.postDelayed(ticker, TICK_MILLIS)
        }
    }

    private fun feedRecorder(newState: VehicleState) {
        state = newState
        recorder.onState(newState)
        records = RecordSnapshot(recorder.trips, recorder.stops, recorder.discardedStops)
    }

    override fun onCleared() {
        handler.removeCallbacks(ticker)
    }
}
