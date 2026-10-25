package com.example.interactive.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 지도 위에 떠 있는 패널 틀 (docs/design/README.md 구조): 가로형은 왼쪽 패널(폭 640, 여백 40),
 * 세로형은 탭 막대 위의 아래 시트. 안이 길면 스크롤한다.
 */
@Composable
fun SidePanel(wide: Boolean, content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val panelModifier = if (wide) {
            Modifier.padding(start = 40.dp, top = 40.dp, bottom = 136.dp).width(640.dp)
        } else {
            Modifier.padding(start = 20.dp, end = 20.dp, bottom = 124.dp).fillMaxWidth().heightIn(max = maxHeight * 0.55f)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = if (wide) Alignment.TopStart else Alignment.BottomCenter) {
            Glass(modifier = panelModifier) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(if (wide) 36.dp else 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content,
                )
            }
        }
    }
}
