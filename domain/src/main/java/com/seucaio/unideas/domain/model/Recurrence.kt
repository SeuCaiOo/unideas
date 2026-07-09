package com.seucaio.unideas.domain.model

import java.time.LocalDate

/**
 * Recurrence of an [Item]. Only meaningful when the item has a [Item.dueDate].
 *
 * A recurring task "renasce ao concluir": completing it spawns a new instance
 * whose due date is [nextDueDate] of the completed one.
 */
enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    ;

    /**
     * Next due date after [from] for this recurrence, or `null` for [NONE].
     */
    fun nextDueDate(from: LocalDate): LocalDate? = when (this) {
        NONE -> null
        DAILY -> from.plusDays(1)
        WEEKLY -> from.plusWeeks(1)
        MONTHLY -> from.plusMonths(1)
    }
}
