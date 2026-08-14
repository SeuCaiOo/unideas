package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import java.time.LocalDateTime

data class ItemOccurrenceUiState(
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
)
