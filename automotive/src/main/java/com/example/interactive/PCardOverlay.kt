package com.example.interactive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.card.PCard
import com.example.interactive.shared.card.PCardKind
import com.example.interactive.shared.card.estimateFuelCostWon
import com.example.interactive.shared.card.formatWon
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.ui.Glass
import com.example.interactive.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WideScreenWidth = 900.dp
private val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)

/**
 * P 카드 (docs/spec.md F3). 기어가 P일 때만 보이고, D·R이 되거나 시스템 주행 규제가 켜지면 바로 숨긴다.
 * 가로형은 왼쪽 패널(카드 + 이번 여행), 세로형은 탭 막대 위의 아래 시트. 칩 아래로 내려 겹치지 않게 한다.
 */
@Composable
fun PCardOverlay(vm: RecordingEngine) {
    val card = vm.pCard ?: return
    val state = vm.state ?: return
    if (state.gear != Gear.P || vm.real()?.uxRestricted == true) return

    val openStop = vm.records.stops.lastOrNull { it.endMillis == null }
    val stayedMinutes = ((state.nowMillis - (openStop?.startMillis ?: state.nowMillis)) / 60_000L).toInt()
    val dongName = card.dongCode?.let { vm.dongLocator.byCode(it)?.name }
    val arrived = openStop?.let { timeFormat.format(Date(it.startMillis)) }
    val header = listOfNotNull(dongName, arrived?.let { "$it 도착" }).joinToString(" · ")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= WideScreenWidth
        if (wide) {
            Column(
                modifier = Modifier.padding(start = 40.dp, top = 136.dp, bottom = 40.dp).width(640.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CardView(card, header, stayedMinutes, wide = true, vm = vm)
                TripSummary(vm)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 124.dp), contentAlignment = Alignment.BottomCenter) {
                CardView(card, header, stayedMinutes, wide = false, vm = vm)
            }
        }
    }
}

@Composable
private fun CardView(card: PCard, header: String, stayedMinutes: Int, wide: Boolean, vm: RecordingEngine) {
    val colors = LocalAppColors.current
    val title = if (wide) 56.sp else 48.sp
    val body = if (wide) 30.sp else 26.sp
    val label = if (wide) 24.sp else 22.sp
    val button = if (wide) 80.dp else 72.dp
    // 영토 카드는 주황, 나머지는 파랑 칩
    val chipColor = if (card.kind == PCardKind.TERRITORY) colors.friendLine else colors.primaryText

    Glass(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(if (wide) 40.dp else 32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    card.kind.chip,
                    fontSize = label,
                    fontWeight = FontWeight.Bold,
                    color = chipColor,
                    modifier = Modifier.clip(CircleShape).background(chipColor.copy(alpha = 0.16f)).padding(horizontal = 18.dp, vertical = 6.dp),
                )
                if (header.isNotEmpty()) Text(header, fontSize = label, color = colors.textSub)
            }
            Text(card.title, fontSize = title, fontWeight = FontWeight.Bold, lineHeight = title * 1.18f, color = colors.text)
            Text(card.body, fontSize = body, lineHeight = body * 1.45f, color = colors.text)
            if (card.showProgress) {
                val shown = stayedMinutes.coerceIn(0, AppConfig.STAY_MIN_MINUTES)
                LinearProgressIndicator(
                    progress = { shown / AppConfig.STAY_MIN_MINUTES.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                    color = colors.primary,
                    trackColor = colors.progressTrack,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                Text("머문 시간 ${shown}분 / ${AppConfig.STAY_MIN_MINUTES}분", fontSize = label, color = colors.textSub)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                val code = card.dongCode
                if (code != null) {
                    Button(
                        onClick = { vm.showDongOnMap(code) },
                        modifier = Modifier.weight(1f).heightIn(min = button),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
                    ) { Text("영토 보기", fontSize = body, fontWeight = FontWeight.Bold) }
                }
                Button(
                    onClick = vm::dismissCard,
                    modifier = Modifier.weight(if (code != null) 0.6f else 1f).heightIn(min = button),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryButton, contentColor = colors.text),
                ) { Text("닫기", fontSize = body, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// "이번 여행": 지금까지 거리·운전 시간·들른 곳·연료비(추정). 가로형에서만 카드 아래에 보인다
@Composable
private fun TripSummary(vm: RecordingEngine) {
    val colors = LocalAppColors.current
    val trip = vm.records.trips.lastOrNull { it.endMillis == null } ?: return
    val state = vm.state ?: return
    val distance = trip.distanceKm(state.odometerKm)
    val stops = vm.records.stops.filter { it.tripId == trip.id }
    val parkedMinutes = stops.sumOf { it.stayedMinutes(state.nowMillis) }
    val driveMinutes = ((state.nowMillis - trip.startMillis) / 60_000L - parkedMinutes).coerceAtLeast(0)
    val fuel = estimateFuelCostWon(distance, vm.settings.efficiency, vm.settings.fuelPriceWonPerLiter)

    Glass(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("이번 여행", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Row(modifier = Modifier.fillMaxWidth()) {
                Stat("달린 거리", "%.0f km".format(distance), Modifier.weight(1f))
                Stat("운전 시간", "${driveMinutes / 60}시간 ${driveMinutes % 60}분", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Stat("들른 곳", "${stops.size}곳", Modifier.weight(1f))
                Stat("연료비 (추정)", "약 ${formatWon(fuel)}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier, size: TextUnit = 34.sp) {
    val colors = LocalAppColors.current
    Column(modifier = modifier) {
        Text(label, fontSize = 24.sp, color = colors.textSub)
        Text(value, fontSize = size, fontWeight = FontWeight.Bold, color = colors.text)
    }
}
