package com.example.interactive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interactive.map.MapScreen
import com.example.interactive.shared.record.TerritoryScorer
import com.example.interactive.shared.vehicle.Gear
import com.example.interactive.ui.Glass
import com.example.interactive.ui.PillShape
import com.example.interactive.ui.TabIcon
import com.example.interactive.ui.TabIconView
import com.example.interactive.ui.theme.AppColors
import com.example.interactive.ui.theme.LocalAppColors

private enum class Tab(val label: String, val icon: TabIcon) {
    MAP("지도", TabIcon.MAP),
    RECORDS("기록", TabIcon.RECORDS),
}

private val WideScreenWidth = 900.dp

/**
 * 화면 뼈대 (docs/design/README.md 구조): 지도가 전체를 채우고, 카드·칩·범례·탭 막대가 위에 떠 있다.
 * 탭 4개(지도·기록·영토·설정)는 11~14단계에서 채운다. 지금은 지도와 임시 확인 화면만 있다.
 */
@Composable
fun AppRoot(vm: RecordingEngine) {
    var tab by rememberSaveable { mutableStateOf(Tab.MAP) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= WideScreenWidth
        when (tab) {
            Tab.MAP -> {
                MapScreen(vm)
                MapChips(vm, wide)
                PCardOverlay(vm)
                if (wide) Legend(Modifier.align(Alignment.BottomEnd).padding(40.dp))
            }
            Tab.RECORDS -> Box(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp)) { DebugScreen(vm) }
        }
        TabBar(tab, wide, onSelect = { tab = it }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}

// 위 왼쪽: 기어 칩("P 주차 중 · 강릉 ○○동"), 위 오른쪽: "내 영토 N곳" 칩
@Composable
private fun MapChips(vm: RecordingEngine, wide: Boolean) {
    val colors = LocalAppColors.current
    val state = vm.state ?: return
    val gearText = when (state.gear) {
        Gear.P -> "주차 중"
        Gear.D -> "주행 중"
        Gear.R -> "후진 중"
        Gear.N -> "중립"
    }
    val dongName = state.location?.let { vm.dongLocator.find(it)?.name }
    val place = if (state.gear == Gear.P && dongName != null) " · 강릉 $dongName" else ""
    val mine = vm.territories.count { it.ownerId == TerritoryScorer.MY_ID }
    val margin = if (wide) 40.dp else 20.dp

    Box(modifier = Modifier.fillMaxSize().padding(margin)) {
        Glass(shape = PillShape, modifier = Modifier.align(Alignment.TopStart)) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 24.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(if (colors.dark) Color.White else Color(0xFF0F1720)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        state.gear.name,
                        fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (colors.dark) Color(0xFF0F1720) else Color.White,
                    )
                }
                Text("$gearText$place", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
            }
        }
        Glass(shape = PillShape, modifier = Modifier.align(Alignment.TopEnd)) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(colors.mineLine))
                Text("내 영토 ${mine}곳", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
            }
        }
    }
}

// 오른쪽 아래 범례 (가로형만)
@Composable
private fun Legend(modifier: Modifier) {
    val colors = LocalAppColors.current
    Glass(modifier = modifier, shape = RoundedCornerShape(36.dp)) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendRow("내 영토", colors, fill = colors.mineFill, line = colors.mineLine, dashed = false)
            LegendRow("다른 사람 영토", colors, fill = colors.friendFill, line = colors.friendLine, dashed = false)
            LegendRow("주인 없음", colors, fill = Color.Transparent, line = colors.noneLine, dashed = true)
        }
    }
}

@Composable
private fun LegendRow(label: String, colors: AppColors, fill: Color, line: Color, dashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Canvas(modifier = Modifier.size(width = 40.dp, height = 28.dp)) {
            val corner = CornerRadius(7.dp.toPx())
            val inset = 1.5.dp.toPx()
            val box = Size(size.width - 2 * inset, size.height - 2 * inset)
            drawRoundRect(fill, topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = box, cornerRadius = corner)
            drawRoundRect(
                line, topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = box, cornerRadius = corner,
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())) else null,
                ),
            )
        }
        Text(label, fontSize = 26.sp, color = colors.text)
    }
}

// 아래 가운데 알약 모양 탭 막대: 선 아이콘 + 글자, 선택된 탭은 파란 배경 (탭 160×88 / 세로형 130×76)
@Composable
private fun TabBar(selected: Tab, wide: Boolean, onSelect: (Tab) -> Unit, modifier: Modifier) {
    val colors = LocalAppColors.current
    Glass(modifier = modifier, shape = PillShape) {
        Row(modifier = Modifier.padding(8.dp)) {
            Tab.entries.forEach { tab ->
                val on = tab == selected
                val tint = if (on) colors.primaryText else colors.textSub
                Column(
                    modifier = Modifier
                        .width(if (wide) 160.dp else 130.dp).height(if (wide) 88.dp else 76.dp)
                        .clip(PillShape)
                        .background(if (on) colors.tabSelectedBg else Color.Transparent)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TabIconView(tab.icon, tint, if (wide) 34.dp else 30.dp)
                    Text(tab.label, fontSize = if (wide) 22.sp else 20.sp, fontWeight = FontWeight.Bold, color = tint)
                }
            }
        }
    }
}
