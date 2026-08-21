package com.seucaio.unideas.domain.model

/**
 * Lifecycle status of an [Item]. An archived item is hidden from the normal listing and its
 * reminders/occurrence generation pause until it's set back to [ACTIVE].
 */
enum class ItemStatus {
    ACTIVE,
    ARCHIVED,
}
