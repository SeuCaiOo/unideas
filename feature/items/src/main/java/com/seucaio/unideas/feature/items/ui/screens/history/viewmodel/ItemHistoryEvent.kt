package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface ItemHistoryEvent {
    data class OnFilterSelected(val filter: HistoryFilter) : ItemHistoryEvent
    data object OnAddEntryClicked : ItemHistoryEvent
    data class OnEditEntryClicked(val entry: ItemCompletionHistory) : ItemHistoryEvent
    data class OnDeleteEntryClicked(val entry: ItemCompletionHistory) : ItemHistoryEvent
    data object OnDeleteConfirmClicked : ItemHistoryEvent
    data object OnDialogDismissed : ItemHistoryEvent
    data class OnEntrySubmitted(
        val scheduledDate: LocalDate,
        val completedAt: LocalDateTime?,
        val note: String?,
    ) : ItemHistoryEvent
}
