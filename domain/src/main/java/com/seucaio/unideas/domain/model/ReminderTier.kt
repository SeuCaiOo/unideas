package com.seucaio.unideas.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Reminder notification tier for an [Item] at a given periodic check, decided independently of
 * [UrgencyLevel] (which only covers the "radar" tier, already surfaced by the priority panel).
 */
enum class ReminderTier {
    NOT_YET,
    NORMAL,
    URGENT,
    ;

    companion object {
        private val DEFAULT_DUE_TIME: LocalTime = LocalTime.of(23, 59)

        /**
         * Derives the reminder tier of [item] at [currentCheck], one of the periodic checks
         * (00h/06h/12h/18h) that scan every item with a due date.
         *
         * @param item must have a non-null [Item.dueDate] — callers filter items without one
         *   before calling this (they have no reminder to compute).
         * @param currentCheck the periodic check being evaluated now.
         * @param nextCheck the next scheduled periodic check after [currentCheck].
         */
        fun of(item: Item, currentCheck: LocalDateTime, nextCheck: LocalDateTime): ReminderTier {
            val dueDate = item.dueDate ?: return NOT_YET
            val effectiveDueDateTime = dueDate.atTime(item.dueTime ?: DEFAULT_DUE_TIME)

            if (!effectiveDueDateTime.isAfter(nextCheck)) return URGENT

            val warning = item.reminderWarning
            if (warning is ReminderWarning.DaysBefore) {
                val warningStart = dueDate.minusDays(warning.days.toLong())
                val today = currentCheck.toLocalDate()
                if (!today.isBefore(warningStart) && !today.isAfter(dueDate)) return NORMAL
            }

            return NOT_YET
        }
    }
}
