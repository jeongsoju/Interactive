package com.example.interactive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

private fun materialColors(c: AppColors) = (if (c.dark) darkColorScheme() else lightColorScheme()).let { base ->
    base.copy(
        primary = c.primary,
        onPrimary = Color.White,
        background = c.land,
        onBackground = c.text,
        surface = if (c.dark) Color(0xFF22262F) else Color.White,
        onSurface = c.text,
        onSurfaceVariant = c.textSub,
    )
}

@Composable
fun InteractiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors(colors),
            typography = PleosTypography,
            content = content
        )
    }
}
