package com.example.interactive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interactive.shared.record.OwnedLand
import com.example.interactive.shared.record.RankEntry
import com.example.interactive.shared.record.TerritoryOverview
import com.example.interactive.shared.record.TerritoryScorer.MY_ID
import com.example.interactive.ui.SidePanel
import com.example.interactive.ui.theme.LocalAppColors

/**
 * 영토 탭 (docs/spec.md 화면 목록): 내 땅 목록과 빼앗길 수 있는 땅, 친구 순위.
 * 땅을 누르면 지도가 그 동으로 간다(지도는 AppRoot가 뒤에 깔아 둔다).
 */
@Composable
fun TerritoryPanel(vm: RecordingEngine, wide: Boolean) {
    val colors = LocalAppColors.current
    val lands = TerritoryOverview.myLands(vm.territories)
    val ranking = TerritoryOverview.ranking(vm.territories, vm.sample.friends.map { it.id })
    fun dongName(code: String) = vm.dongLocator.byCode(code)?.name ?: code
    fun personName(id: String) = if (id == MY_ID) "나" else vm.sample.friends.firstOrNull { it.id == id }?.name ?: id

    SidePanel(wide) {
        val atRisk = lands.count { it.atRisk }
        Text("내 영토 ${lands.size}곳", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = colors.text)
        if (atRisk > 0) Text("그중 ${atRisk}곳은 빼앗길 수 있어요", fontSize = 26.sp, color = colors.friendLine, fontWeight = FontWeight.Bold)
        if (lands.isEmpty()) Text("아직 내 땅이 없어요. 한 곳에 10분 넘게 머물면 내 땅이 돼요.", fontSize = 26.sp, color = colors.textSub)
        lands.forEach { land -> LandRow(land, ::dongName, ::personName) { vm.showDongOnMap(land.dongCode) } }

        Text("친구 순위", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.padding(top = 18.dp))
        val most = ranking.maxOfOrNull { it.lands }?.coerceAtLeast(1) ?: 1
        ranking.forEachIndexed { index, entry -> RankRow(index + 1, entry, most, ::personName) }
    }
}

@Composable
private fun LandRow(land: OwnedLand, dongName: (String) -> String, personName: (String) -> String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.text.copy(alpha = 0.06f))
            .clickable(onClick = onClick).padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(dongName(land.dongCode), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.text)
            val rival = land.rivalId?.let { "${personName(it)} ${land.rivalScore}점" }
            Text("내 점수 ${land.myScore}점" + (rival?.let { " · $it" } ?: ""), fontSize = 24.sp, color = colors.textSub)
        }
        if (land.atRisk) {
            Text(
                "빼앗길 수 있어요",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.friendLine,
                modifier = Modifier.clip(CircleShape).background(colors.friendFill).padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun RankRow(place: Int, entry: RankEntry, most: Int, personName: (String) -> String) {
    val colors = LocalAppColors.current
    val mine = entry.personId == MY_ID
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(if (mine) colors.primary else colors.text.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) { Text("$place", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (mine) Color.White else colors.text) }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(personName(entry.personId), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
            // 땅이 가장 많은 사람을 가득 채우고 나머지는 그 비율로 막대를 그린다
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(colors.progressTrack)) {
                Box(
                    modifier = Modifier.fillMaxWidth(entry.lands / most.toFloat()).height(10.dp).clip(CircleShape)
                        .background(if (mine) colors.mineLine else colors.friendLine),
                )
            }
        }
        Text("${entry.lands}곳", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
    }
}
