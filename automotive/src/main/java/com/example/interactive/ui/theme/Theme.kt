package com.example.interactive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Pleos 디자인 가이드는 색 HEX를 공개하지 않는다. 가이드 그림의 무채색 계열을 근사한 임시 값.
private val LightColors = lightColorScheme(
    primary = Color(0xFF43464E),
    onPrimary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF131417),
    surface = Color.White,
    onSurface = Color(0xFF131417),
    onSurfaceVariant = Color(0xFF5C5F66),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE3E4E8),
    onPrimary = Color(0xFF131417),
    background = Color(0xFF1C1D20),
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF131417),
    onSurface = Color(0xFFF2F3F5),
    onSurfaceVariant = Color(0xFFA9ACB3),
)

// Pleos 디자인 가이드 글자 크기 (docs/pleos/ui-design-guide.md)
private val PleosTypography = Typography(
    headlineLarge = TextStyle(fontSize = 60.sp, lineHeight = 80.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 56.sp, lineHeight = 72.sp, fontWeight = FontWeight.ExtraBold),
    headlineSmall = TextStyle(fontSize = 48.sp, lineHeight = 64.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 40.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 30.sp, lineHeight = 38.sp),
    bodyMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
    bodySmall = TextStyle(fontSize = 26.sp, lineHeight = 34.sp),
    labelLarge = TextStyle(fontSize = 24.sp, lineHeight = 28.sp),
    labelMedium = TextStyle(fontSize = 24.sp, lineHeight = 28.sp),
    labelSmall = TextStyle(fontSize = 20.sp, lineHeight = 24.sp),
)

@Composable
fun InteractiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PleosTypography,
        content = content
    )
}
