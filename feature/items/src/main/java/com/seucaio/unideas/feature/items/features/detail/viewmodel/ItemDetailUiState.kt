package com.seucaio.unideas.feature.items.features.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.features.form.screen.components.ItemFormFieldsState
import java.time.LocalDate

/**
 * UI state for the add-item screen. No `Loading`/`Error` — the fields are always there, blank
 * until data arrives. Loading the section/tag reference list is a background concern (already
 * degrades to an empty list on failure inside
 * [com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase], never surfaced here) — nothing
 * about typing a title depends on it.
 */
data class ItemDetailUiState(
    override val type: ItemType = ItemType.TASK,
    override val title: String = "",
    override val description: String = "",
    override val sectionId: Long? = null,
    override val selectedTagIds: Set<Long> = emptySet(),
    override val dueDate: LocalDate? = null,
    override val recurrence: Recurrence = Recurrence.None,
    override val availableSections: List<Section> = emptyList(),
    override val availableTags: List<Tag> = emptyList(),
) : ItemFormFieldsState {

    override val isTitleValid: Boolean get() = title.isNotBlank()

    override val canPickRecurrence: Boolean get() = dueDate != null

    override val typeIsTask: Boolean get() = type == ItemType.TASK

    fun changeType(type: ItemType): ItemDetailUiState = copy(type = type)

    fun changeTitle(title: String): ItemDetailUiState = copy(title = title)

    fun changeDescription(description: String): ItemDetailUiState = copy(description = description)

    fun setSection(sectionId: Long?): ItemDetailUiState = copy(sectionId = sectionId)

    fun setTag(tagId: Long): ItemDetailUiState = copy(
        selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId
    )
}
