package com.seucaio.unideas.core.ui.components

/**
 * `:core:ui` cannot depend on `:domain` (see ARCHITECTURE.md), so this mirrors
 * `domain.UrgencyLevel` locally — the feature layer maps one to the other.
 */
enum class UrgencyIndicatorLevel {
    OVERDUE,
    DUE_SOON,
    NORMAL,
}
