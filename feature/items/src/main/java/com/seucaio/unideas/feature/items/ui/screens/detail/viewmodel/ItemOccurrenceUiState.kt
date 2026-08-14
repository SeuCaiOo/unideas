package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import java.time.LocalDate
import java.time.LocalDateTime

data class ItemOccurrenceUiState(
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val dueDate: LocalDate? = null,
) {

    val isLate: Boolean
        get() = !isCompleted && dueDate != null && LocalDate.now().isAfter(dueDate)
}
