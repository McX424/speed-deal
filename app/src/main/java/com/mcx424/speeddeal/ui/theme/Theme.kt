package com.mcx424.speeddeal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = HighlightCool,
    onPrimary = Background,
    secondary = TextSecondary,
    onSecondary = Background,
    tertiary = HighlightCool,
    background = Background,
    onBackground = TextPrimary,
    surface = Background,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLow = SurfaceCard,
    surfaceContainer = SurfaceCard,
    surfaceContainerHigh = SurfaceCard,
    outline = Border,
    outlineVariant = Border,
    error = TimeUpRed,
    onError = Background
)

@Composable
fun SpeedDealTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
