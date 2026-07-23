package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import java.time.LocalDate

/** User interactions on the add-item screen. */
sealed interface AddItemEvent {

    data class OnTypeChanged(val type: ItemType) : AddItemEvent

    data class OnTitleChanged(val title: String) : AddItemEvent

    data class OnDescriptionChanged(val description: String) : AddItemEvent

    data class OnSectionChanged(val sectionId: Long?) : AddItemEvent

    data class OnTagToggled(val tagId: Long) : AddItemEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : AddItemEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : AddItemEvent

    data object OnSaveClicked : AddItemEvent
}
