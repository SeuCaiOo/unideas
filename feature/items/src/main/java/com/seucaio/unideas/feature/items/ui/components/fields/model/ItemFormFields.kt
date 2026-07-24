package com.seucaio.unideas.feature.items.ui.components.fields.model

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate

interface ItemFormFieldsState {
    val type: ItemType
    val title: String
    val description: String
    val sectionId: Long?
    val selectedTagIds: Set<Long>
    val dueDate: LocalDate?
    val recurrence: Recurrence
    val availableSections: List<Section>
    val availableTags: List<Tag>
    val isTitleValid: Boolean
    val canPickRecurrence: Boolean
    val typeIsTask: Boolean
    val isCompleted: Boolean get() = false
    val isEditing: Boolean get() = false
}

data class ItemFormFieldsEvents(
    val onTypeChanged: (ItemType) -> Unit,
    val onTitleChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onSectionChanged: (Long?) -> Unit,
    val onTagToggled: (Long) -> Unit,
    val onDueDateChanged: (LocalDate?) -> Unit,
    val onRecurrenceChanged: (Recurrence) -> Unit,
    val onSaveClicked: () -> Unit,
)
