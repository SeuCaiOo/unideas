package com.seucaio.unideas.feature.items.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate

/** UI state for the create/edit item form. */
sealed interface ItemFormUiState {

    data object Loading : ItemFormUiState

    data class Success(
        val isEditing: Boolean,
        val type: ItemType = ItemType.TASK,
        val title: String = "",
        val description: String = "",
        val sectionId: Long? = null,
        val selectedTagIds: Set<Long> = emptySet(),
        val dueDate: LocalDate? = null,
        val recurrence: Recurrence = Recurrence.None,
        val availableSections: List<Section> = emptyList(),
        val availableTags: List<Tag> = emptyList(),
    ) : ItemFormUiState {

        val isTitleValid: Boolean get() = title.isNotBlank()

        val canPickRecurrence: Boolean get() = dueDate != null
    }

    data class Error(@StringRes val messageRes: Int) : ItemFormUiState
}
