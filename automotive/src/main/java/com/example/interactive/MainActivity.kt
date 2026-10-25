package com.example.interactive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.shared.vehicle.ManualVehicleDataSource
import com.example.interactive.shared.vehicle.VehicleState
import com.example.interactive.ui.theme.InteractiveTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ButtonHeight = 64.dp

class MainActivity : ComponentActivity() {
    private val viewModel: VehicleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InteractiveTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (viewModel.choosing) ModeChooser(viewModel) else DebugScreen(viewModel)
                }
            }
        }
    }
}

// ---- 임시 화면: 모드 선택창 ----

@Composable
fun ModeChooser(vm: VehicleViewModel) {
    var pending by remember { mutableStateOf<AppMode?>(null) }
    val current = vm.mode

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Interactive", style = MaterialTheme.typography.headlineMedium)
        Text("어떤 모드로 시작할까요?", style = MaterialTheme.typography.titleLarge)
        ChooserButton("시연 모드", "가짜 여행이 저절로 재생돼요") { pending = AppMode.REPLAY }
        ChooserButton("직접 조작 모드", "버튼으로 기어·위치·시간을 바꿔요") { pending = AppMode.MANUAL }
        if (current != null) {
            OutlinedButton(onClick = vm::hideChooser, modifier = Modifier.fillMaxWidth().heightIn(min = ButtonHeight)) {
                Text("취소")
            }
        }
    }

    pending?.let { picked ->
        // 처음 고르거나 같은 모드를 다시 고르면 묻지 않는다
        if (current == null || picked == current) {
            pending = null
            if (current == null) vm.chooseMode(picked, resetSample = false) else vm.hideChooser()
        } else {
            AlertDialog(
                onDismissRequest = { pending = null },
                title = { Text("샘플 데이터로 초기화할까요?") },
                text = { Text("${picked.label}로 바꿉니다.") },
                confirmButton = {
                    TextButton(
                        onClick = { vm.chooseMode(picked, resetSample = true); pending = null },
                        modifier = Modifier.heightIn(min = ButtonHeight),
                    ) { Text("초기화하고 바꾸기") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { vm.chooseMode(picked, resetSample = false); pending = null },
                        modifier = Modifier.heightIn(min = ButtonHeight),
                    ) { Text("그대로 바꾸기") }
                },
            )
        }
    }
}

@Composable
private fun ChooserButton(title: String, caption: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(caption, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val AppMode.label get() = if (this == AppMode.REPLAY) "시연 모드" else "직접 조작 모드"

// ---- 임시 화면: 지금 차량 값 + 모드별 조작 ----

private val timeFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.KOREA)

@Composable
fun DebugScreen(vm: VehicleViewModel) {
    val state = vm.state ?: return
    val sample = vm.sample
    val nearestDong = state.location?.let { loc ->
        sample.dongs.minByOrNull {
            val dLat = it.center.latitude - loc.latitude
            val dLon = it.center.longitude - loc.longitude
            dLat * dLat + dLon * dLon
        }?.name
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("${vm.mode?.label}", style = MaterialTheme.typography.headlineMedium)
        OutlinedButton(onClick = vm::showChooser, modifier = Modifier.heightIn(min = ButtonHeight)) {
            Text("모드 바꾸기")
        }

        StateText(state, nearestDong)
        Text(
            "샘플 데이터: 동 ${sample.dongs.size}곳 · 지난 여행 ${sample.pastTrips.size}개 · " +
                "친구 ${sample.friends.joinToString { it.name }} · 영토 ${sample.territories.size}곳",
            style = MaterialTheme.typography.bodyMedium,
        )

        when (vm.mode) {
            AppMode.REPLAY -> ReplayControls(vm)
            AppMode.MANUAL -> vm.manual()?.let { ManualPanel(it, vm) }
            null -> {}
        }
    }
}

@Composable
private fun StateText(state: VehicleState, nearestDong: String?) {
    val gearText = when (state.gear) {
        Gear.P -> "P (주차)"
        Gear.R -> "R (후진)"
        Gear.N -> "N (중립)"
        Gear.D -> "D (주행)"
    }
    val loc = state.location
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("기어  $gearText", style = MaterialTheme.typography.titleLarge)
        Text(
            "위치  ${loc?.let { "%.4f, %.4f".format(it.latitude, it.longitude) } ?: "없음"}" +
                (nearestDong?.let { "  (가장 가까운 동: $it)" } ?: ""),
            style = MaterialTheme.typography.titleLarge,
        )
        Text("주행거리  %.1f km".format(state.odometerKm), style = MaterialTheme.typography.titleLarge)
        Text("탑승 인원  ${state.occupants}명", style = MaterialTheme.typography.titleLarge)
        Text("앱 시각  ${timeFormat.format(Date(state.nowMillis))}", style = MaterialTheme.typography.titleLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReplayControls(vm: VehicleViewModel) {
    Text("재생 속도", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1, 10, 60).forEach { speed ->
            ChoiceButton("${speed}배", selected = vm.replaySpeed == speed) { vm.changeReplaySpeed(speed) }
        }
        OutlinedButton(onClick = vm::restartReplay, modifier = Modifier.heightIn(min = ButtonHeight)) {
            Text("처음부터")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManualPanel(manual: ManualVehicleDataSource, vm: VehicleViewModel) {
    var open by rememberSaveable { mutableStateOf(true) }
    val state = vm.state ?: return

    OutlinedButton(onClick = { open = !open }, modifier = Modifier.fillMaxWidth().heightIn(min = ButtonHeight)) {
        Text(if (open) "직접 조작 패널 접기" else "직접 조작 패널 펼치기")
    }
    if (!open) return

    val spacing = Arrangement.spacedBy(8.dp)

    Text("기어", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = spacing, verticalArrangement = spacing) {
        Gear.entries.forEach { gear -> ChoiceButton(gear.name, state.gear == gear) { manual.setGear(gear) } }
    }

    Text("위치 (강릉 동)", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = spacing, verticalArrangement = spacing) {
        vm.sample.dongs.forEach { dong ->
            ChoiceButton(dong.name, state.location == dong.center) { manual.setLocation(dong.center) }
        }
    }

    Text("시간 넘기기", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = spacing, verticalArrangement = spacing) {
        ActionButton("+10분") { manual.advanceTime(10 * 60_000L) }
        ActionButton("+1시간") { manual.advanceTime(60 * 60_000L) }
        ActionButton("+1일") { manual.advanceTime(24 * 60 * 60_000L) }
    }

    Text("탑승 인원", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = spacing, verticalArrangement = spacing) {
        (1..4).forEach { n -> ChoiceButton("${n}명", state.occupants == n) { manual.setOccupants(n) } }
    }

    Text("주행거리", style = MaterialTheme.typography.titleMedium)
    ActionButton("+10km") { manual.addOdometer(10.0) }

    ActionButton("데이터 초기화") { vm.resetData() }
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.heightIn(min = ButtonHeight)) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.heightIn(min = ButtonHeight)) { Text(label) }
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.heightIn(min = ButtonHeight)) { Text(label) }
}
