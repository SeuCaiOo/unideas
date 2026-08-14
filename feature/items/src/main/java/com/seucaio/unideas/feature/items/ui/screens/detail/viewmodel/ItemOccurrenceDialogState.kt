package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

sealed interface ItemOccurrenceDialogState {

    data object None : ItemOccurrenceDialogState

    data object ReopenConfirm : ItemOccurrenceDialogState

    data object History : ItemOccurrenceDialogState
}
