package com.seucaio.unideas.ds.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The product is dark-only by design — no light scheme is offered.
private val DsColorScheme = darkColorScheme(
    background = Background,
    surface = Surface1,
    surfaceVariant = Surface2,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = AccentContainer,
    onPrimaryContainer = OnAccentContainer,
    secondary = Accent,
    onSecondary = OnAccent,
    outline = Outline,
    error = Danger,
    onError = OnAccent
)

@Composable
fun DsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DsColorScheme,
        typography = AppTypography,
        content = content
    )
}
