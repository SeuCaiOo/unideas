package com.seucaio.unideas.domain.model

import java.time.LocalDate

/**
 * Urgency derived from [Item.dueDate] versus today — never persisted.
 *
 * `dueDate < today` → [OVERDUE] · `dueDate <= today + N` → [DUE_SOON] · otherwise [NORMAL].
 */
enum class UrgencyLevel {
    OVERDUE,
    DUE_SOON,
    NORMAL,
    ;

    companion object {
        /**
         * Derives the urgency of a due date.
         *
         * @param dueDate the item's due date; `null` means no deadline → [NORMAL].
         * @param today the reference date (injected for testability).
         * @param dueSoonDays the "due soon" threshold in days (comes from the caller's
         *   configuration — this module takes it as a parameter on purpose).
         */
        fun of(dueDate: LocalDate?, today: LocalDate, dueSoonDays: Int): UrgencyLevel = when {
            dueDate == null -> NORMAL
            dueDate.isBefore(today) -> OVERDUE
            !dueDate.isAfter(today.plusDays(dueSoonDays.toLong())) -> DUE_SOON
            else -> NORMAL
        }
    }
}
