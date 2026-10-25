package com.example.interactive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interactive.shared.AppConfig
import com.example.interactive.shared.card.estimateFuelCostWon
import com.example.interactive.shared.card.formatWon
import com.example.interactive.shared.model.Stop
import com.example.interactive.shared.record.TerritoryScorer
import com.example.interactive.shared.record.TripEntry
import com.example.interactive.ui.SidePanel
import com.example.interactive.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREA)
private val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)

/**
 * 기록 탭 (docs/spec.md 화면 목록): 여행 목록(날짜별 여행·거리·연료비)과 여행 상세(정차 지점 목록).
 * 여행을 고르면 지도에 경로와 정차 지점이 그려진다(지도는 AppRoot가 뒤에 깔아 둔다).
 * 가로형은 왼쪽 패널, 세로형은 탭 막대 위의 아래 시트.
 */
@Composable
fun RecordsPanel(vm: RecordingEngine, entries: List<TripEntry>, selectedKey: String?, onSelect: (String?) -> Unit, wide: Boolean) {
    val selected = entries.firstOrNull { it.key == selectedKey }
    SidePanel(wide) {
        if (selected == null) TripList(vm, entries, onSelect) else TripDetail(vm, selected) { onSelect(null) }
    }
}

@Composable
private fun TripList(vm: RecordingEngine, entries: List<TripEntry>, onSelect: (String?) -> Unit) {
    val colors = LocalAppColors.current
    Text("여행 기록", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = colors.text)
    if (entries.isEmpty()) {
        Text("아직 여행이 없어요. 차가 출발하면 자동으로 기록돼요.", fontSize = 28.sp, color = colors.textSub)
        return
    }
    // 날짜(기기 시간대 기준)가 같은 여행끼리 묶는다
    entries.groupBy { dateFormat.format(Date(it.trip.startMillis)) }.forEach { (date, trips) ->
        Text(date, fontSize = 24.sp, color = colors.textSub, modifier = Modifier.padding(top = 6.dp))
        trips.forEach { entry ->
            val dongs = entry.stops.sortedBy { it.startMillis }.mapNotNull { s -> s.dongCode?.let { vm.dongLocator.byCode(it)?.name } }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(colors.text.copy(alpha = 0.06f)).clickable { onSelect(entry.key) }.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (dongs.isEmpty()) "이동 기록" else dongs.distinct().joinToString(" → "),
                        fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.weight(1f, fill = false),
                    )
                    if (entry.live) Chip("진행 중")
                }
                Text(tripMeta(vm, entry, withStops = true), fontSize = 24.sp, color = colors.textSub)
            }
        }
    }
}

@Composable
private fun TripDetail(vm: RecordingEngine, entry: TripEntry, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onBack,
            modifier = Modifier.heightIn(min = 64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryButton, contentColor = colors.text),
        ) { Text("← 목록", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        Text(dateFormat.format(Date(entry.trip.startMillis)), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colors.text)
        if (entry.live) Chip("진행 중")
    }
    Text(tripMeta(vm, entry, withStops = false), fontSize = 26.sp, color = colors.textSub)

    Text("들른 곳", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.padding(top = 8.dp))
    val stops = entry.stops.sortedBy { it.startMillis }
    if (stops.isEmpty()) Text("10분 이상 머문 곳이 없어요.", fontSize = 26.sp, color = colors.textSub)
    stops.forEachIndexed { index, stop -> StopRow(vm, index + 1, stop) }
}

@Composable
private fun StopRow(vm: RecordingEngine, number: Int, stop: Stop) {
    val colors = LocalAppColors.current
    val name = stop.dongCode?.let { vm.dongLocator.byCode(it)?.name } ?: if (stop.latitude == null) "위치 없음" else "경계 밖"
    val mine = vm.territories.firstOrNull { it.dongCode == stop.dongCode }?.ownerId == TerritoryScorer.MY_ID
    val now = vm.state?.nowMillis ?: stop.startMillis
    val end = stop.endMillis?.let { timeFormat.format(Date(it)) } ?: "지금"
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.text.copy(alpha = 0.06f)).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.primary), contentAlignment = Alignment.Center) {
            Text("$number", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(name, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.text)
                if (mine) Chip("내 땅")
            }
            Text(
                "${timeFormat.format(Date(stop.startMillis))} – $end · 체류 ${stop.stayedMinutes(now)}분 · ${stop.occupants}명",
                fontSize = 22.sp, color = colors.textSub,
            )
        }
    }
}

// 두 줄: "오후 3:12 – 오후 5:40" / "142 km · 연료비 약 1만 9천 원 (추정) · 들른 곳 3곳"
private fun tripMeta(vm: RecordingEngine, entry: TripEntry, withStops: Boolean): String {
    val trip = entry.trip
    val nowMillis = vm.state?.nowMillis ?: trip.startMillis
    val distance = if (entry.live) trip.distanceKm(vm.state?.odometerKm) else trip.distanceKm()
    val fuel = trip.fuelCostWon ?: estimateFuelCostWon(
        distance, vm.settings.efficiency, vm.settings.fuelPriceWonPerLiter
    )
    val time = "${timeFormat.format(Date(trip.startMillis))} – " +
        if (entry.live) "지금" else timeFormat.format(Date(trip.endMillis ?: nowMillis))
    val parts = mutableListOf("%.0f km".format(distance), "연료비 약 ${formatWon(fuel)} (추정)")
    if (withStops) parts += "들른 곳 ${entry.stops.size}곳" else parts += "${entry.stops.size}곳 들름"
    return time + "\n" + parts.joinToString(" · ")
}

@Composable
private fun Chip(text: String) {
    val colors = LocalAppColors.current
    Text(
        text,
        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.primaryText,
        modifier = Modifier.clip(CircleShape).background(colors.tabSelectedBg).padding(horizontal = 16.dp, vertical = 5.dp),
    )
}
