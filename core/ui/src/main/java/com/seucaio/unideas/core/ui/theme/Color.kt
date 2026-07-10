package com.seucaio.unideas.core.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 — teal seed. Dark scheme.
val PrimaryDark = Color(0xFF4FD8C4)
val OnPrimaryDark = Color(0xFF00382E)
val PrimaryContainerDark = Color(0xFF00504A)
val OnPrimaryContainerDark = Color(0xFF71F5DE)
val BackgroundDark = Color(0xFF191C1B)
val OnBackgroundDark = Color(0xFFE1E3E1)
val SurfaceDark = Color(0xFF191C1B)
val OnSurfaceDark = Color(0xFFE1E3E1)
val SurfaceVariantDark = Color(0xFF3F4947)
val OnSurfaceVariantDark = Color(0xFFBFC9C6)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)
val OutlineDark = Color(0xFF899391)

// Material 3 — teal seed. Light scheme.
val PrimaryLight = Color(0xFF006A60)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF74F8E5)
val OnPrimaryContainerLight = Color(0xFF00201C)
val BackgroundLight = Color(0xFFFAFDFB)
val OnBackgroundLight = Color(0xFF191C1B)
val SurfaceLight = Color(0xFFFAFDFB)
val OnSurfaceLight = Color(0xFF191C1B)
val SurfaceVariantLight = Color(0xFFDAE5E2)
val OnSurfaceVariantLight = Color(0xFF3F4947)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)
val OutlineLight = Color(0xFF6F7976)

/**
 * Colors reserved exclusively for due-date urgency ([UrgencyIndicatorLevel]) — never
 * reused elsewhere in the UI, per the project's visual rules. Same values in both
 * schemes — chosen with enough saturation/contrast to read on both.
 */
object UrgencyColors {
    val Overdue = Color(0xFFFF5449)
    val DueSoon = Color(0xFFFFB300)
}
