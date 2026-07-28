package com.seucaio.unideas.domain.model

/**
 * Configures whether an [Item] gets a "normal" tier reminder notification before its due date.
 * Only meaningful when the item has a [Item.dueDate]. See [ReminderTier.of].
 */
sealed interface ReminderWarning {

    data object None : ReminderWarning

    /** Warn starting [days] days before the due date. */
    data class DaysBefore(val days: Int) : ReminderWarning {

        init {
            require(days > 0) { "days must be positive" }
        }
    }
}
