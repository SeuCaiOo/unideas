package com.seucaio.unideas.core.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 dark scheme — teal seed.
val Primary = Color(0xFF4FD8C4)
val OnPrimary = Color(0xFF00382E)
val PrimaryContainer = Color(0xFF00504A)
val OnPrimaryContainer = Color(0xFF71F5DE)
val BackgroundColor = Color(0xFF191C1B)
val OnBackgroundColor = Color(0xFFE1E3E1)
val SurfaceColor = Color(0xFF191C1B)
val OnSurfaceColor = Color(0xFFE1E3E1)
val SurfaceVariant = Color(0xFF3F4947)
val OnSurfaceVariant = Color(0xFFBFC9C6)
val ErrorColor = Color(0xFFFFB4AB)
val OnErrorColor = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)
val Outline = Color(0xFF899391)

/**
 * Colors reserved exclusively for due-date urgency ([UrgencyIndicatorLevel]) — never
 * reused elsewhere in the UI, per the project's visual rules.
 */
object UrgencyColors {
    val Overdue = Color(0xFFFF5449)
    val DueSoon = Color(0xFFFFB300)
}
