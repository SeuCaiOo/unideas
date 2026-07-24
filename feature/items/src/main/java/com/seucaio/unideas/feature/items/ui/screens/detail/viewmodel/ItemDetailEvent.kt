package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import java.time.LocalDate

sealed interface ItemDetailEvent {

    data class OnTypeChanged(val type: ItemType) : ItemDetailEvent

    data class OnTitleChanged(val title: String) : ItemDetailEvent

    data class OnDescriptionChanged(val description: String) : ItemDetailEvent

    data class OnSectionChanged(val sectionId: Long?) : ItemDetailEvent

    data class OnTagToggled(val tagId: Long) : ItemDetailEvent

    data class OnDueDateChanged(val dueDate: LocalDate?) : ItemDetailEvent

    data class OnRecurrenceChanged(val recurrence: Recurrence) : ItemDetailEvent

    data object OnSaveClicked : ItemDetailEvent

    data object OnShareClicked : ItemDetailEvent

    data object OnDeleteClicked : ItemDetailEvent

    data object OnDeleteConfirmClicked : ItemDetailEvent

    data object OnDialogDismissed : ItemDetailEvent

    data object OnCompleteClicked : ItemDetailEvent

    /** Retry loading the item after [ItemDetailUiState.loadFailed] — edit mode only. */
    data object OnRetryClicked : ItemDetailEvent
}
