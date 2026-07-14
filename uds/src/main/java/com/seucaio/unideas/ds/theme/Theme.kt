package com.seucaio.unideas.ds.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val UdsDarkColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface = Surface1Dark,
    surfaceVariant = Surface2Dark,
    surfaceContainerHigh = Surface3Dark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    primary = AccentDark,
    onPrimary = OnAccentDark,
    primaryContainer = AccentContainerDark,
    onPrimaryContainer = OnAccentContainerDark,
    secondary = AccentDark,
    onSecondary = OnAccentDark,
    outline = OutlineDark,
    outlineVariant = DividerColorDark,
    error = DangerDark,
    onError = OnAccentDark,
)

private val UdsLightColorScheme = lightColorScheme(
    background = BackgroundLight,
    surface = Surface1Light,
    surfaceVariant = Surface2Light,
    surfaceContainerHigh = Surface3Light,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    primary = AccentLight,
    onPrimary = OnAccentLight,
    primaryContainer = AccentContainerLight,
    onPrimaryContainer = OnAccentContainerLight,
    secondary = AccentLight,
    onSecondary = OnAccentLight,
    outline = OutlineLight,
    outlineVariant = DividerColorLight,
    error = DangerLight,
    onError = OnAccentLight,
)

@Composable
fun UdsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalUdsExtendedColors provides if (darkTheme) UdsExtendedColorsDark else UdsExtendedColorsLight,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) UdsDarkColorScheme else UdsLightColorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
