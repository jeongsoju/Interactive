package com.example.interactive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

enum class TabIcon { MAP, RECORDS, TERRITORY, SETTINGS }

/** 탭 막대의 선 아이콘 (24칸 격자에 그린 단순한 선 그림. 아이콘 라이브러리를 쓰지 않는다) */
@Composable
fun TabIconView(icon: TabIcon, color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val unit = this.size.width / 24f
        val stroke = Stroke(width = 2f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * unit, y * unit)
        when (icon) {
            TabIcon.MAP -> {
                val outline = Path().apply {
                    moveTo(3 * unit, 6 * unit); lineTo(9 * unit, 4 * unit); lineTo(15 * unit, 6 * unit); lineTo(21 * unit, 4 * unit)
                    lineTo(21 * unit, 18 * unit); lineTo(15 * unit, 20 * unit); lineTo(9 * unit, 18 * unit); lineTo(3 * unit, 20 * unit)
                    close()
                }
                drawPath(outline, color, style = stroke)
                drawLine(color, p(9f, 4f), p(9f, 18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(15f, 6f), p(15f, 20f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TabIcon.RECORDS -> {
                drawCircle(color, radius = 9 * unit, center = p(12f, 12f), style = stroke)
                drawLine(color, p(12f, 7f), p(12f, 12f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(12f, 12f), p(15.5f, 14f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TabIcon.TERRITORY -> {
                // 깃발: 기둥 + 펄럭이는 천
                drawLine(color, p(6f, 3f), p(6f, 21f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                val flag = Path().apply {
                    moveTo(6 * unit, 4 * unit); lineTo(19 * unit, 4 * unit); lineTo(15 * unit, 8.5f * unit); lineTo(19 * unit, 13 * unit); lineTo(6 * unit, 13 * unit)
                }
                drawPath(flag, color, style = stroke)
            }
            TabIcon.SETTINGS -> {
                // 슬라이더 두 줄
                drawLine(color, p(4f, 8f), p(20f, 8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(4f, 16f), p(20f, 16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(color, radius = 2.6f * unit, center = p(9f, 8f), style = stroke)
                drawCircle(color, radius = 2.6f * unit, center = p(15f, 16f), style = stroke)
            }
        }
    }
}
