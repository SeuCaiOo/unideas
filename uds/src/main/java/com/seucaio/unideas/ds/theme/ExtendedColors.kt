package com.seucaio.unideas.ds.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens with no direct Material 3 [androidx.compose.material3.ColorScheme] slot — everything
 * else (background/surface/text/accent/error/outline) maps onto standard M3 roles in
 * [DsTheme] instead. Read via [LocalDsExtendedColors] so nested composables resolve the same
 * dark/light choice [DsTheme] already made, instead of re-deriving it themselves.
 */
data class DsExtendedColors(
    val textTertiary: Color,
    val warning: Color,
    val panelBackground: Color,
    val panelBorder: Color,
    val snackbarBackground: Color,
    val snackbarContent: Color,
)

val DsExtendedColorsDark = DsExtendedColors(
    textTertiary = TextTertiaryDark,
    warning = WarningDark,
    panelBackground = PanelBackgroundDark,
    panelBorder = PanelBorderDark,
    snackbarBackground = SnackbarBackgroundDark,
    snackbarContent = SnackbarContentDark,
)

val DsExtendedColorsLight = DsExtendedColors(
    textTertiary = TextTertiaryLight,
    warning = WarningLight,
    panelBackground = PanelBackgroundLight,
    panelBorder = PanelBorderLight,
    snackbarBackground = SnackbarBackgroundLight,
    snackbarContent = SnackbarContentLight,
)

val LocalDsExtendedColors = staticCompositionLocalOf { DsExtendedColorsDark }
