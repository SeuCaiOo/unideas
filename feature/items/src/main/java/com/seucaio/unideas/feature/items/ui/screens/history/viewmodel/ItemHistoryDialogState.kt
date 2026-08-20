package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import java.time.LocalDate

sealed interface ItemHistoryDialogState {
    data object None : ItemHistoryDialogState

    data class AddEditEntry(
        val existing: ItemCompletionHistory? = null,
        val blockedDates: Set<LocalDate> = emptySet(),
    ) : ItemHistoryDialogState

    data class DeleteConfirm(val entry: ItemCompletionHistory) : ItemHistoryDialogState
}
