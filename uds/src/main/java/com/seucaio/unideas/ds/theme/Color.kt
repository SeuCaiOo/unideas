package com.seucaio.unideas.ds.theme

import androidx.compose.ui.graphics.Color

// Design tokens — dark scheme (variant 1a / Tonal), the module's original palette.
val BackgroundDark = Color(0xFF141218)
val Surface1Dark = Color(0xFF1D1B20) // form fields
val Surface2Dark = Color(0xFF211F26) // item cards, list cards (Tonal)
val Surface3Dark = Color(0xFF2B2930) // icon hover, neutral status chips
val OutlineDark = Color(0xFF49454F) // field borders, unselected chips
val DividerColorDark = Color(0x0FFFFFFF) // rgba(255,255,255,.06) row dividers

val TextPrimaryDark = Color(0xFFE6E0E9)
val TextSecondaryDark = Color(0xFFCAC4D0)
val TextTertiaryDark = Color(0xFF938F99)

val AccentDark = Color(0xFF7FD8C3)
val OnAccentDark = Color(0xFF00382E)
val AccentContainerDark = Color(0xFF2E4B42)
val OnAccentContainerDark = Color(0xFFB9EFDC)

val DangerDark = Color(0xFFFFB4AB)
val WarningDark = Color(0xFFE8C26C)

val PanelBackgroundDark = Color(0xFF1F2C27)
val PanelBorderDark = Color(0xFF2E453D)

val ScrimColorDark = Color(0x8C000000) // rgba(0,0,0,.55)
val RowHoverColorDark = Color(0x0AFFFFFF) // rgba(255,255,255,.04)
val SnackbarBackgroundDark = TextPrimaryDark
val SnackbarContentDark = Surface1Dark

// Design tokens — light scheme, teal seed matching :core:ui's UnideasTheme (same accent
// family, so components look consistent whichever theme module renders a given screen
// during the :core:ui -> :uds migration).
val BackgroundLight = Color(0xFFFAFDFB)
val Surface1Light = Color(0xFFF3F6F4)
val Surface2Light = Color(0xFFECF0EE)
val Surface3Light = Color(0xFFE3E8E6)
val OutlineLight = Color(0xFF6F7976)
val DividerColorLight = Color(0x14000000) // rgba(0,0,0,.08) row dividers

val TextPrimaryLight = Color(0xFF191C1B)
val TextSecondaryLight = Color(0xFF3F4947)
val TextTertiaryLight = Color(0xFF6F7976)

val AccentLight = Color(0xFF006A60)
val OnAccentLight = Color(0xFFFFFFFF)
val AccentContainerLight = Color(0xFF74F8E5)
val OnAccentContainerLight = Color(0xFF00201C)

val DangerLight = Color(0xFFBA1A1A)
val WarningLight = Color(0xFF7A5900)

val PanelBackgroundLight = Color(0xFFE3F2ED)
val PanelBorderLight = Color(0xFFB6DED0)

val ScrimColorLight = Color(0x8C000000)
val RowHoverColorLight = Color(0x0A000000) // rgba(0,0,0,.04)
val SnackbarBackgroundLight = TextPrimaryLight
val SnackbarContentLight = BackgroundLight
