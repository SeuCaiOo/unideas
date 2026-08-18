package com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import java.time.LocalDate
import java.time.LocalTime

sealed interface ItemDetailEvent {

    sealed interface FieldEvent : ItemDetailEvent

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

    data object OnRetryClicked : ItemDetailEvent

    data object OnBackRequested : ItemDetailEvent

    data object OnDiscardConfirmed : ItemDetailEvent

    data class OnItemUpdatedExternally(val item: Item) : ItemDetailEvent

    data object OnScreenResumed : ItemDetailEvent
}
