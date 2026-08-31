package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel

import java.time.LocalDate
import java.time.LocalDateTime

data class ItemOccurrenceUiState(
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val dueDate: LocalDate? = null,
    val isRecurring: Boolean = false,
    val remindersMuted: Boolean = false,
    val hasHistory: Boolean = false,
    val historyCount: Int = 0,
    val historyOnTimePercent: Int = 0,
) {

    val isLate: Boolean
        get() = !isCompleted && dueDate != null && LocalDate.now().isAfter(dueDate)

    val completedLate: Boolean
        get() = isCompleted && !isRecurring && dueDate != null && completedAt != null &&
            completedAt.toLocalDate().isAfter(dueDate)

    val canIgnore: Boolean get() = isLate && isRecurring
}
