package com.seucaio.unideas.feature.items.features.form.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate

/**
 * UI state for the create/edit item form. No `Loading`/`Error` — the screen itself never fails
 * to render: the fields are always there, blank until data arrives. Loading a section/tag
 * reference list is a background concern (already degrades to an empty list on failure inside
 * [com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase], never surfaced here) — nothing
 * about typing a title depends on it. The one real failure (editing an item that no longer
 * exists) has nothing to render either way, so it's a one-shot [ItemFormUiAction.ShowError] +
 * navigate-back, not a screen state.
 */
data class ItemFormUiState(
    val isEditing: Boolean = false,
    val type: ItemType = ItemType.TASK,
    val title: String = "",
    val description: String = "",
    val sectionId: Long? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val dueDate: LocalDate? = null,
    val recurrence: Recurrence = Recurrence.None,
    val availableSections: List<Section> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val isCompleted: Boolean = false,
) {

    val isTitleValid: Boolean get() = title.isNotBlank()

    val canPickRecurrence: Boolean get() = dueDate != null

    val typeIsTask: Boolean get() = type == ItemType.TASK
}
