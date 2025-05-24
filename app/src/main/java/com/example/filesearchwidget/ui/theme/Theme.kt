package com.example.filesearchwidget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB39DDB),             // Light Purple
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),    // Very light purple
    onPrimaryContainer = Color(0xFF311B92),  // Deep purple text

    secondary = Color(0xFF7E57C2),           // Muted Purple
    onSecondary = Color.White,

    background = Color(0xFFFAF9FF),          // Soft near-white purple-tinted background
    onBackground = Color(0xFF1A1B1F),        // Neutral dark text

    surface = Color.White,                   // Cards/sheets
    onSurface = Color(0xFF1A1B1F),           // Normal text
    surfaceVariant = Color(0xFFF3E5F5),      // Light purple surface variant
    onSurfaceVariant = Color(0xFF5F5B66),    // Supporting text
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80D8FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFB2DFDB),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1F1F1F),
    onSurface = Color.White,
)

@Composable
fun FileSearchTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}