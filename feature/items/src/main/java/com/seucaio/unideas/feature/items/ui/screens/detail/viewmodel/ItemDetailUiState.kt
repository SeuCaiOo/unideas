package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import java.time.LocalDate
import java.time.LocalDateTime

data class ItemDetailUiState(
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
    override val completedAt: LocalDateTime? = null,
    val loadFailed: Boolean = false,
) : ItemFormFieldsState {

    override val isTitleValid: Boolean get() = title.isNotBlank()

    override val canPickRecurrence: Boolean get() = dueDate != null

    override val typeIsTask: Boolean get() = type == ItemType.TASK
}
