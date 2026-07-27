package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import java.time.LocalDate
import java.time.LocalTime

sealed interface AddItemEvent {

    data class OnTypeChanged(val type: ItemType) : AddItemEvent

    data class OnTitleChanged(val title: String) : AddItemEvent

    data class OnDescriptionChanged(val description: String) : AddItemEvent

    data class OnSectionChanged(val sectionId: Long?) : AddItemEvent

    data class OnTagToggled(val tagId: Long) : AddItemEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : AddItemEvent

    data class OnDueTimeChanged(val dueTime: LocalTime?) : AddItemEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : AddItemEvent

    data class OnReminderWarningChanged(val reminderWarning: ReminderWarning) : AddItemEvent

    data object OnSaveClicked : AddItemEvent
}
