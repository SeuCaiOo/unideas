package com.seucaio.unideas.feature.items.features.form.screen.components

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate

/**
 * Field values common to every screen that renders [ItemFormBody] — implemented by both
 * `ItemFormUiState` (create/edit) and `ItemDetailUiState` (create-only), so the body/fields don't
 * couple to either ViewModel's own state type. [isCompleted]/[isEditing] default to `false` since
 * only `ItemFormUiState` has a real notion of either.
 */
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

/** Field callbacks common to every screen that renders [ItemFormBody] — see [ItemFormFieldsState]. */
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
