package com.triwahyu.miwatchu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyberBlack = Color(0xFF0F111A)
val CyberSurface = Color(0xFF1A1C29)
val NeonCyan = Color(0xFF00E5FF)
val NeonAmber = Color(0xFFFFB300)
val NeonGreen = Color(0xFF00E676)
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFF9E9E9E)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonAmber,
    tertiary = NeonGreen,
    background = CyberBlack,
    surface = CyberSurface,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MiWatchUTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
