package com.seucaio.unideas.ds.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens with no direct Material 3 [androidx.compose.material3.ColorScheme] slot — everything
 * else (background/surface/text/accent/error/outline) maps onto standard M3 roles in
 * [UdsTheme] instead. Read via [LocalUdsExtendedColors] so nested composables resolve the same
 * dark/light choice [UdsTheme] already made, instead of re-deriving it themselves.
 */
data class UdsExtendedColors(
    val textTertiary: Color,
    val warning: Color,
    val panelBackground: Color,
    val panelBorder: Color,
    val snackbarBackground: Color,
    val snackbarContent: Color,
)

val UdsExtendedColorsDark = UdsExtendedColors(
    textTertiary = TextTertiaryDark,
    warning = WarningDark,
    panelBackground = PanelBackgroundDark,
    panelBorder = PanelBorderDark,
    snackbarBackground = SnackbarBackgroundDark,
    snackbarContent = SnackbarContentDark,
)

val UdsExtendedColorsLight = UdsExtendedColors(
    textTertiary = TextTertiaryLight,
    warning = WarningLight,
    panelBackground = PanelBackgroundLight,
    panelBorder = PanelBorderLight,
    snackbarBackground = SnackbarBackgroundLight,
    snackbarContent = SnackbarContentLight,
)

val LocalUdsExtendedColors = staticCompositionLocalOf { UdsExtendedColorsDark }
