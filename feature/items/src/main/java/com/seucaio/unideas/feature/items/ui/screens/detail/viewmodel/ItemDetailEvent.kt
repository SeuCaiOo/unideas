package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import java.time.LocalDate
import java.time.LocalTime

sealed interface ItemDetailEvent {

    data class OnTypeChanged(val type: ItemType) : ItemDetailEvent

    data class OnTitleChanged(val title: String) : ItemDetailEvent

    data class OnDescriptionChanged(val description: String) : ItemDetailEvent

    data class OnSectionChanged(val sectionId: Long?) : ItemDetailEvent

    data class OnTagToggled(val tagId: Long) : ItemDetailEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : ItemDetailEvent

    data class OnDueTimeChanged(val dueTime: LocalTime?) : ItemDetailEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : ItemDetailEvent

    data class OnReminderWarningChanged(val reminderWarning: ReminderWarning) : ItemDetailEvent

    data object OnSaveClicked : ItemDetailEvent

    data object OnShareClicked : ItemDetailEvent

    data object OnDeleteClicked : ItemDetailEvent

    data object OnDeleteConfirmClicked : ItemDetailEvent

    data object OnDialogDismissed : ItemDetailEvent

    data object OnCompleteClicked : ItemDetailEvent

    data object OnCompleteConfirmClicked : ItemDetailEvent

    data object OnHistoryClicked : ItemDetailEvent

    /** Retry loading the item after [ItemDetailUiState.loadFailed] — edit mode only. */
    data object OnRetryClicked : ItemDetailEvent
}
