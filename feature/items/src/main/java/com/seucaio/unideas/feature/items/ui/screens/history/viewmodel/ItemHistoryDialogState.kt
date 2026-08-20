package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import com.seucaio.unideas.domain.model.ItemCompletionHistory

sealed interface ItemHistoryDialogState {
    data object None : ItemHistoryDialogState
    data class AddEditEntry(val existing: ItemCompletionHistory? = null) : ItemHistoryDialogState
    data class DeleteConfirm(val entry: ItemCompletionHistory) : ItemHistoryDialogState
}
