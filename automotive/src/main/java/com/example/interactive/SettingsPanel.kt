package com.example.interactive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interactive.shared.model.CarModels
import com.example.interactive.ui.SidePanel
import com.example.interactive.ui.theme.LocalAppColors

/**
 * 설정 탭 (docs/spec.md F6): 차종(공인연비), 유가, 우리 집 동네, 기록 전체 삭제, 모드 전환.
 * 아래에는 시연·직접 조작 도구(개발용)와 데이터 출처 표시가 있다. 친구 관리는 12/3 시연 단계에서 붙인다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsPanel(vm: RecordingEngine, wide: Boolean) {
    val colors = LocalAppColors.current
    val settings = vm.settings
    var confirmDelete by remember { mutableStateOf(false) }
    var showTools by remember { mutableStateOf(false) }

    SidePanel(wide) {
        Text("설정", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = colors.text)

        Section("차종 (공인연비)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CarModels.examples.forEach { car ->
                Pill(car.name, selected = settings.carModel == car.name) { vm.changeSettings { withCar(car) } }
            }
        }
        Hint(
            (settings.carModel?.let { "$it · " } ?: "차종을 고르지 않아 ") + "공인연비 %.1f km/L".format(settings.efficiency) +
                " (예시 값, 공식 값 아님)",
        )

        Section("유가 (휘발유)")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Pill("− 50원", selected = false) { vm.changeSettings { withPriceChange(-50) } }
            Text("%,d원/L".format(settings.fuelPriceWonPerLiter), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Pill("+ 50원", selected = false) { vm.changeSettings { withPriceChange(50) } }
        }
        Hint("연료비 = 달린 거리 ÷ 공인연비 × 유가. 화면에는 (추정)으로 표시해요.")

        Section("우리 집 동네")
        Hint("여기서 머문 시간은 영토 점수에 넣지 않아요.")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill("없음", selected = settings.homeDongCode == null) { vm.changeSettings { copy(homeDongCode = null) } }
            vm.dongs.forEach { dong ->
                Pill(dong.name, selected = settings.homeDongCode == dong.code) { vm.changeSettings { copy(homeDongCode = dong.code) } }
            }
        }

        Section("모드")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(vm.mode?.label ?: "", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Pill("모드 바꾸기", selected = false, onClick = vm::showChooser)
        }

        Section("기록")
        Button(
            onClick = { confirmDelete = true },
            modifier = Modifier.heightIn(min = 72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.friendFill, contentColor = colors.friendLine),
        ) { Text("기록 전체 삭제", fontSize = 28.sp, fontWeight = FontWeight.Bold) }

        Section("개발용 조작")
        Pill(if (showTools) "접기" else "시연·직접 조작 도구 열기", selected = false) { showTools = !showTools }
        if (showTools) ControlsBlock(vm)

        Section("정보")
        Hint("지도: OpenFreeMap © OpenMapTiles, Data from OpenStreetMap")
        Hint("행정동 경계: 통계청 SGIS · vuski/admdongkor (CC BY 4.0)")
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("기록을 모두 지울까요?") },
            text = { Text("여행·정차 기록과 영토 점수가 처음(샘플) 상태로 돌아가요. 설정은 그대로예요.") },
            confirmButton = {
                TextButton(onClick = { vm.resetData(); confirmDelete = false }, modifier = Modifier.heightIn(min = 64.dp)) { Text("모두 지우기") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, modifier = Modifier.heightIn(min = 64.dp)) { Text("취소") }
            },
        )
    }
}

@Composable
private fun Section(title: String) {
    Text(title, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.text, modifier = Modifier.padding(top = 14.dp))
}

@Composable
private fun Hint(text: String) {
    Text(text, fontSize = 24.sp, color = LocalAppColors.current.textSub)
}

// 알약 모양 선택 버튼: 선택되면 파란 배경 (높이 64로 누르기 쉽게)
@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Text(
        label,
        fontSize = 26.sp, fontWeight = FontWeight.Bold,
        color = if (selected) Color.White else colors.text,
        modifier = Modifier.heightIn(min = 64.dp).clip(CircleShape)
            .background(if (selected) colors.primary else colors.text.copy(alpha = 0.08f))
            .clickable(onClick = onClick).padding(horizontal = 26.dp, vertical = 14.dp),
    )
}
