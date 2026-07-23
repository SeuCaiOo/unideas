package com.seucaio.unideas.feature.items.features.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate

/**
 * UI state for the add-item screen. No `Loading`/`Error` — the fields are always there, blank
 * until data arrives. Loading the section/tag reference list is a background concern (already
 * degrades to an empty list on failure inside
 * [com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase], never surfaced here) — nothing
 * about typing a title depends on it.
 */
data class ItemDetailUiState(
    val type: ItemType = ItemType.TASK,
    val title: String = "",
    val description: String = "",
    val sectionId: Long? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val dueDate: LocalDate? = null,
    val recurrence: Recurrence = Recurrence.None,
    val availableSections: List<Section> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
) {

    val isTitleValid: Boolean get() = title.isNotBlank()

    val canPickRecurrence: Boolean get() = dueDate != null

    val typeIsTask: Boolean get() = type == ItemType.TASK

    fun changeType(type: ItemType): ItemDetailUiState = copy(type = type)

    fun changeTitle(title: String): ItemDetailUiState = copy(title = title)

    fun changeDescription(description: String): ItemDetailUiState = copy(description = description)

    fun setSection(sectionId: Long?): ItemDetailUiState = copy(sectionId = sectionId)

    fun setTag(tagId: Long): ItemDetailUiState = copy(
        selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId
    )
}
