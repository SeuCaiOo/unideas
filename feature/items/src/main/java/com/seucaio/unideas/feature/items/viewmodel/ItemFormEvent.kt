package com.seucaio.unideas.feature.items.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import java.time.LocalDate

/** User interactions on the create/edit item form. */
sealed interface ItemFormEvent {

    data class OnTypeChanged(val type: ItemType) : ItemFormEvent

    data class OnTitleChanged(val title: String) : ItemFormEvent

    data class OnDescriptionChanged(val description: String) : ItemFormEvent

    data class OnSectionChanged(val sectionId: Long?) : ItemFormEvent

    data class OnTagToggled(val tagId: Long) : ItemFormEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : ItemFormEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : ItemFormEvent

    data object OnSaveClicked : ItemFormEvent

    data object OnRetryClicked : ItemFormEvent
}
