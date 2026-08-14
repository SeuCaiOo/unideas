package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel

import java.time.LocalDate

sealed interface ItemOccurrenceDialogState {

    data object None : ItemOccurrenceDialogState

    data object ReopenConfirm : ItemOccurrenceDialogState

    data class CompleteConfirm(val isLate: Boolean) : ItemOccurrenceDialogState

    data object IgnoreConfirm : ItemOccurrenceDialogState

    data class ExtendDeadlineConfirm(val currentDueDate: LocalDate) : ItemOccurrenceDialogState

    data object History : ItemOccurrenceDialogState
}
