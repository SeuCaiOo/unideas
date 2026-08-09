package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import java.time.LocalDate
import java.time.LocalTime

sealed interface AddItemEvent {

    /** Events handled by [AddItemUiState.reduce] alone — no side effect beyond a state update. */
    sealed interface FieldEvent : AddItemEvent

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

    data object OnSaveClicked : AddItemEvent
}
