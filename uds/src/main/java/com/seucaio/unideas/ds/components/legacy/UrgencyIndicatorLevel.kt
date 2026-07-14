package com.seucaio.unideas.ds.components.legacy

/**
 * `:uds` cannot depend on `:domain` (portability rule, see the module's README), so this
 * mirrors `domain.UrgencyLevel` locally — the feature layer maps one to the other.
 */
enum class UrgencyIndicatorLevel {
    OVERDUE,
    DUE_SOON,
    NORMAL,
}
