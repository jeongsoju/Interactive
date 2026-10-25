package com.example.interactive.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** docs/design/README.md "색"·"글래스" 표의 값. 화면은 이 값만 쓰고 색을 직접 적지 않는다 */
data class AppColors(
    val land: Color,
    val water: Color,
    val text: Color,
    val textSub: Color,
    val primary: Color,
    /** 선택된 탭·칩의 글자색 */
    val primaryText: Color,
    val tabSelectedBg: Color,
    val mineFill: Color,
    val mineLine: Color,
    val friendFill: Color,
    val friendLine: Color,
    val noneLine: Color,
    val glass: Color,
    val glassBorder: Color,
    /** 닫기 같은 보조 버튼 바탕 */
    val secondaryButton: Color,
    val progressTrack: Color,
    val dark: Boolean,
)

val LightAppColors = AppColors(
    land = Color(0xFFECEFF2),
    water = Color(0xFFCFE2F3),
    text = Color(0xFF0F1720),
    textSub = Color(0xFF4A5563),
    primary = Color(0xFF2F6BFF),
    primaryText = Color(0xFF1F55E0),
    tabSelectedBg = Color(0x242F6BFF), // 14%
    mineFill = Color(0x382F6BFF), // 22%
    mineLine = Color(0xFF2F6BFF),
    friendFill = Color(0x38F08A24), // 22%
    friendLine = Color(0xFFD9700E),
    noneLine = Color(0xFF9AA5B1),
    glass = Color(0xEEFFFFFF), // 흰색 93% (뒤 흐림이 없어서 시안의 58~66%보다 진하게)
    glassBorder = Color(0xCCFFFFFF), // 흰색 80%
    secondaryButton = Color(0xCCFFFFFF),
    progressTrack = Color(0x1F0F1720),
    dark = false,
)

val DarkAppColors = AppColors(
    land = Color(0xFF171B22),
    water = Color(0xFF0F2236),
    text = Color(0xFFF2F4F7),
    textSub = Color(0xFFB4BDC8),
    primary = Color(0xFF2F6BFF),
    primaryText = Color(0xFFA9C2FF),
    tabSelectedBg = Color(0x336E9BFF), // 20%
    mineFill = Color(0x3D5B8CFF), // 24%
    mineLine = Color(0xFF6E9BFF),
    friendFill = Color(0x33F5A04A), // 20%
    friendLine = Color(0xFFF5A04A),
    noneLine = Color(0xFF6B7683),
    glass = Color(0xEE22262F), // #22262F 93% (뒤 흐림이 없어서 시안의 60~66%보다 진하게)
    glassBorder = Color(0x24FFFFFF), // 흰색 14%
    secondaryButton = Color(0xFF2F3440),
    progressTrack = Color(0x33FFFFFF),
    dark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
