package com.example.interactive.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interactive.ui.theme.LocalAppColors

/**
 * 지도 위에 떠 있는 패널 틀 (docs/design/README.md 구조): 가로형은 왼쪽 패널(폭 640, 여백 40),
 * 세로형은 탭 막대 위의 아래 시트. 위쪽 제목 줄을 누르면 제목만 남기고 접히며, 제목 옆 ▼/▲ 표시로 펼칠 수 있다.
 * 접힘 상태는 key마다 따로 LocalCollapsedPanels에 저장돼서 탭을 옮겼다 와도 유지된다(AppRoot가 제공). 안이 길면 제목 아래에서 스크롤한다.
 */
@Composable
fun SidePanel(wide: Boolean, title: String, key: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalAppColors.current
    val states = LocalCollapsedPanels.current
    val collapsed = states[key] == true
    val pad = if (wide) 36.dp else 28.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val panelModifier = if (wide) {
            Modifier.padding(start = 40.dp, top = 40.dp, bottom = 136.dp).width(640.dp)
        } else {
            Modifier.padding(start = 20.dp, end = 20.dp, bottom = 124.dp).fillMaxWidth().heightIn(max = maxHeight * 0.55f)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = if (wide) Alignment.TopStart else Alignment.BottomCenter) {
            Glass(modifier = panelModifier.animateContentSize()) {
                Column(modifier = Modifier.padding(pad)) {
                    // 제목 줄: 누르면 접기/펼치기
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { states[key] = !collapsed },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(title, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.weight(1f, fill = false))
                        Chevron(down = collapsed, color = colors.textSub)
                    }
                    if (!collapsed) {
                        Column(
                            modifier = Modifier.weight(1f, fill = false).padding(top = 14.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            content = content,
                        )
                    }
                }
            }
        }
    }
}

// 펼침 표시: 접혀 있으면 ▼(눌러서 더보기), 펼쳐져 있으면 ▲(눌러서 접기). 선으로 그린 꺾쇠
@Composable
private fun Chevron(down: Boolean, color: Color) {
    Canvas(modifier = Modifier.padding(start = 16.dp).size(40.dp)) {
        val u = size.width / 24f
        val stroke = Stroke(width = 2.6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val topY = if (down) 8f else 16f
        val tipY = if (down) 16f else 8f
        drawLine(color, Offset(5f * u, topY * u), Offset(12f * u, tipY * u), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(12f * u, tipY * u), Offset(19f * u, topY * u), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

/** 패널(key)별 접힘 상태. AppRoot가 앱이 떠 있는 동안 유지되는 값을 넣어 준다 */
val LocalCollapsedPanels = compositionLocalOf<SnapshotStateMap<String, Boolean>> { mutableStateMapOf() }
