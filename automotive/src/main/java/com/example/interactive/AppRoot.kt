package com.example.interactive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.interactive.map.MapScreen

private enum class Tab(val label: String) { MAP("지도"), RECORDS("기록") }

/**
 * 화면 뼈대: 지도가 전체를 채우고, 탭 막대는 아래 가운데에 떠 있다 (docs/design/README.md 구조).
 * 탭 4개(지도·기록·영토·설정)는 11~14단계에서 채운다. 지금은 지도와 임시 확인 화면만 있다.
 */
@Composable
fun AppRoot(vm: RecordingEngine) {
    var tab by rememberSaveable { mutableStateOf(Tab.MAP) }
    Box(modifier = Modifier.fillMaxSize()) {
        when (tab) {
            Tab.MAP -> MapScreen(vm)
            Tab.RECORDS -> Box(modifier = Modifier.fillMaxSize().padding(bottom = 112.dp)) { DebugScreen(vm) }
        }
        TabBar(tab, onSelect = { tab = it }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}

@Composable
private fun TabBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier) {
    Surface(modifier = modifier, shape = CircleShape, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Tab.entries.forEach { tab ->
                val on = tab == selected
                Box(
                    modifier = Modifier
                        .width(160.dp).height(72.dp)
                        .clip(CircleShape)
                        .background(if (on) Color(0xFF2F6BFF).copy(alpha = 0.14f) else Color.Transparent)
                        .clickable { onSelect(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (on) Color(0xFF1F55E0) else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
