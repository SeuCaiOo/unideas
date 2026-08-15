package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import java.time.LocalDate
import java.time.LocalTime

sealed interface ItemConfigEvent {

    sealed interface FieldEvent : ItemConfigEvent

    data class OnSectionChanged(val sectionId: Long?) : FieldEvent
    data class OnTagToggled(val tagId: Long) : FieldEvent
    data class OnReminderToggled(val enabled: Boolean) : FieldEvent
    data class OnDueDateChanged(val dueDate: LocalDate?) : FieldEvent
    data class OnDueTimeChanged(val dueTime: LocalTime?) : FieldEvent
    data class OnRecurrenceChanged(val recurrence: Recurrence) : FieldEvent
    data class OnReminderWarningChanged(val reminderWarning: ReminderWarning) : FieldEvent

    data class OnChangeTypeClicked(val newType: ItemType) : ItemConfigEvent
    data object OnTypeSwitchConfirmClicked : ItemConfigEvent
    data object OnDialogDismissed : ItemConfigEvent
    data object OnRetryClicked : ItemConfigEvent
}
