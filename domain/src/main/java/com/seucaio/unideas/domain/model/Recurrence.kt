package com.seucaio.unideas.domain.model

import java.io.Serializable
import java.time.LocalDate

sealed interface Recurrence : Serializable {

    /**
     * Next due date after [from] for this recurrence, or `null` for [None].
     */
    fun nextDueDate(from: LocalDate): LocalDate?

    data object None : Recurrence {
        override fun nextDueDate(from: LocalDate): LocalDate? = null
    }

    data object Daily : Recurrence {
        override fun nextDueDate(from: LocalDate): LocalDate = from.plusDays(1)
    }

    data object Weekly : Recurrence {
        override fun nextDueDate(from: LocalDate): LocalDate = from.plusWeeks(1)
    }

    data object Monthly : Recurrence {
        override fun nextDueDate(from: LocalDate): LocalDate = from.plusMonths(1)
    }

    /** Custom interval in [days] (e.g. biweekly, every other day). */
    data class EveryNDays(val days: Int) : Recurrence {

        init {
            require(days > 0) { "days must be positive" }
        }

        override fun nextDueDate(from: LocalDate): LocalDate = from.plusDays(days.toLong())

        companion object {
            const val BIWEEKLY_DAYS = 15
            const val EVERY_OTHER_DAY_DAYS = 2
        }
    }
}
