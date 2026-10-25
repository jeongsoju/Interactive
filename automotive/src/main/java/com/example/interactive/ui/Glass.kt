package com.example.interactive.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.interactive.ui.theme.LocalAppColors

/**
 * 지도 위에 떠 있는 반투명 카드 (docs/design/README.md "글래스"). 뒤 흐림(블러)은 아직 없이
 * 반투명 배경 + 테두리 + 그림자로 만든다. 모서리: 카드 44, 작은 칩은 알약(999).
 */
@Composable
fun Glass(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(44.dp),
    content: @Composable () -> Unit,
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.glass,
        border = BorderStroke(1.dp, colors.glassBorder),
        shadowElevation = 12.dp,
    ) { Box { content() } }
}

val PillShape = RoundedCornerShape(999.dp)
