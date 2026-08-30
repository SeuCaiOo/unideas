package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel

import com.seucaio.unideas.domain.model.Item
import java.time.LocalDate

sealed interface ItemOccurrenceEvent {

    data object OnCompleteClicked : ItemOccurrenceEvent

    data object OnCompleteConfirmClicked : ItemOccurrenceEvent

    data class OnCompleteWithNoteConfirmClicked(val note: String?) : ItemOccurrenceEvent

    data object OnIgnoreClicked : ItemOccurrenceEvent

    data class OnIgnoreConfirmClicked(val note: String) : ItemOccurrenceEvent

    data object OnExtendDeadlineClicked : ItemOccurrenceEvent

    data class OnExtendDeadlineConfirmClicked(val newDueDate: LocalDate) : ItemOccurrenceEvent

    data object OnMuteRemindersToggled : ItemOccurrenceEvent

    data object OnDialogDismissed : ItemOccurrenceEvent

    data class OnItemUpdatedExternally(val item: Item) : ItemOccurrenceEvent
}
