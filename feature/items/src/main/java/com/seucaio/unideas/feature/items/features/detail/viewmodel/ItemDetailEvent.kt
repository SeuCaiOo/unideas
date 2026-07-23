package com.seucaio.unideas.feature.items.features.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import java.time.LocalDate

/** User interactions on the add-item screen. */
sealed interface ItemDetailEvent {

    data class OnTypeChanged(val type: ItemType) : ItemDetailEvent

    data class OnTitleChanged(val title: String) : ItemDetailEvent

    data class OnDescriptionChanged(val description: String) : ItemDetailEvent

    data class OnSectionChanged(val sectionId: Long?) : ItemDetailEvent

    data class OnTagToggled(val tagId: Long) : ItemDetailEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : ItemDetailEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : ItemDetailEvent

    data object OnSaveClicked : ItemDetailEvent
}
