package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import java.time.LocalDate
import java.time.LocalTime

sealed interface ItemDetailEvent {

    /** Events handled by [ItemDetailUiState.reduce] alone — no side effect beyond a state update. */
    sealed interface FieldEvent : ItemDetailEvent

    data class OnTypeChanged(val type: ItemType) : FieldEvent

    data class OnTitleChanged(val title: String) : FieldEvent

    data class OnDescriptionChanged(val description: String) : FieldEvent

    data class OnSectionChanged(val sectionId: Long?) : FieldEvent

    data class OnTagToggled(val tagId: Long) : FieldEvent

    data class OnReminderToggled(val enabled: Boolean) : FieldEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : FieldEvent

    data class OnDueTimeChanged(val dueTime: LocalTime?) : FieldEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : FieldEvent

    data class OnReminderWarningChanged(val reminderWarning: ReminderWarning) : FieldEvent

    data object OnShareClicked : ItemDetailEvent

    data object OnDeleteClicked : ItemDetailEvent

    data object OnDeleteConfirmClicked : ItemDetailEvent

    data object OnDialogDismissed : ItemDetailEvent

    data object OnCompleteClicked : ItemDetailEvent

    data object OnCompleteConfirmClicked : ItemDetailEvent

    data object OnHistoryClicked : ItemDetailEvent

    /** Retry loading the item after [ItemDetailUiState.loadFailed] — edit mode only. */
    data object OnRetryClicked : ItemDetailEvent

    data object OnBackRequested : ItemDetailEvent

    data object OnDiscardConfirmed : ItemDetailEvent
}
