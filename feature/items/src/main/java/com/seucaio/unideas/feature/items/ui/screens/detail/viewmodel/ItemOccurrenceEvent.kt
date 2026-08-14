package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import com.seucaio.unideas.domain.model.Item

sealed interface ItemOccurrenceEvent {

    data object OnCompleteClicked : ItemOccurrenceEvent

    data object OnCompleteConfirmClicked : ItemOccurrenceEvent

    data class OnCompleteWithNoteConfirmClicked(val note: String?) : ItemOccurrenceEvent

    data object OnIgnoreClicked : ItemOccurrenceEvent

    data class OnIgnoreConfirmClicked(val note: String) : ItemOccurrenceEvent

    data object OnHistoryClicked : ItemOccurrenceEvent

    data object OnDialogDismissed : ItemOccurrenceEvent

    data class OnItemUpdatedExternally(val item: Item) : ItemOccurrenceEvent
}
