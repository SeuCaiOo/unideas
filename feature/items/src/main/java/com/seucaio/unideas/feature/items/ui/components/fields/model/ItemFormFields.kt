package com.seucaio.unideas.feature.items.ui.components.fields.model

import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate
import java.time.LocalTime

interface ItemFormFieldsState {
    val type: ItemType
    val title: String
    val description: String
    val sectionId: Long?
    val selectedTagIds: Set<Long>
    val dueDate: LocalDate?
    val dueTime: LocalTime?
    val recurrence: Recurrence
    val reminderWarning: ReminderWarning
    val availableSections: List<Section>
    val availableTags: List<Tag>
    val isTitleValid: Boolean
    val hasReminder: Boolean
    val typeIsTask: Boolean
    val isEditing: Boolean get() = false
    val titleError: Boolean get() = false
}

/** [dueDate] with no reminder scheduled — persisted state never carries a stale, hidden date. */
val ItemFormFieldsState.persistableDueDate: LocalDate? get() = if (hasReminder) dueDate else null

/** [dueTime] with no reminder scheduled — persisted state never carries a stale, hidden time. */
val ItemFormFieldsState.persistableDueTime: LocalTime? get() = if (hasReminder) dueTime else null

/** [recurrence] with no reminder scheduled — persisted state never carries a stale recurrence rule. */
val ItemFormFieldsState.persistableRecurrence: Recurrence get() = if (hasReminder) recurrence else Recurrence.None

/** [reminderWarning] with no reminder scheduled — persisted state never carries a stale warning. */
val ItemFormFieldsState.persistableReminderWarning: ReminderWarning
    get() = if (hasReminder) reminderWarning else ReminderWarning.None

data class ItemFormFieldsEvents(
    val onTypeChanged: (ItemType) -> Unit,
    val onTitleChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onSectionChanged: (Long?) -> Unit,
    val onTagToggled: (Long) -> Unit,
    val onReminderToggled: (Boolean) -> Unit,
    val onDueDateChanged: (LocalDate?) -> Unit,
    val onDueTimeChanged: (LocalTime?) -> Unit,
    val onRecurrenceChanged: (Recurrence) -> Unit,
    val onReminderWarningChanged: (ReminderWarning) -> Unit,
)
