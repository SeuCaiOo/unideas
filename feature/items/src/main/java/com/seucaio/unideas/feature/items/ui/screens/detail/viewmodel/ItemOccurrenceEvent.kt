package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

sealed interface ItemOccurrenceEvent {

    data object OnCompleteClicked : ItemOccurrenceEvent

    data object OnCompleteConfirmClicked : ItemOccurrenceEvent

    data object OnHistoryClicked : ItemOccurrenceEvent

    data object OnDialogDismissed : ItemOccurrenceEvent
}
