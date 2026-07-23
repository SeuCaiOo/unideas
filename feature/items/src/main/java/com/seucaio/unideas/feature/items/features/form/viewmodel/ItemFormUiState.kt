package com.seucaio.unideas.feature.items.features.form.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.features.form.screen.components.ItemFormFieldsState
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
    override val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    override val type: ItemType = ItemType.TASK,
    override val title: String = "",
    override val description: String = "",
    override val sectionId: Long? = null,
    override val selectedTagIds: Set<Long> = emptySet(),
    override val dueDate: LocalDate? = null,
    override val recurrence: Recurrence = Recurrence.None,
    override val availableSections: List<Section> = emptyList(),
    override val availableTags: List<Tag> = emptyList(),
    override val isCompleted: Boolean = false,
    val loadFailed: Boolean = false,
) : ItemFormFieldsState {

    override val isTitleValid: Boolean get() = title.isNotBlank()

    override val canPickRecurrence: Boolean get() = dueDate != null

    override val typeIsTask: Boolean get() = type == ItemType.TASK
}
