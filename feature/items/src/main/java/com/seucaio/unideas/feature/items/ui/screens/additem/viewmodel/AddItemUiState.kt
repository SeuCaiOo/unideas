package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import com.seucaio.unideas.core.common.extensions.orToday
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import java.time.LocalDate
import java.time.LocalTime

data class AddItemUiState(
    override val type: ItemType = ItemType.TASK,
    override val title: String = "",
    override val description: String = "",
    override val sectionId: Long? = null,
    override val selectedTagIds: Set<Long> = emptySet(),
    override val hasReminder: Boolean = false,
    override val dueDate: LocalDate? = null,
    override val dueTime: LocalTime? = null,
    override val recurrence: Recurrence = Recurrence.None,
    override val reminderWarning: ReminderWarning = ReminderWarning.None,
    override val availableSections: List<Section> = emptyList(),
    override val availableTags: List<Tag> = emptyList(),
) : ItemFormFieldsState {

    override val isTitleValid: Boolean get() = title.isNotBlank()

    override val typeIsTask: Boolean get() = type == ItemType.TASK

    fun changeType(type: ItemType): AddItemUiState = copy(type = type)

    fun changeTitle(title: String): AddItemUiState = copy(title = title)

    fun changeDescription(description: String): AddItemUiState = copy(description = description)

    fun setSection(sectionId: Long?): AddItemUiState = copy(sectionId = sectionId)

    fun setTag(tagId: Long): AddItemUiState = copy(
        selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId
    )

    fun setReferenceData(sections: List<Section>, tags: List<Tag>): AddItemUiState =
        copy(availableSections = sections, availableTags = tags)

    fun toggleReminder(enabled: Boolean): AddItemUiState = if (enabled) {
        copy(hasReminder = true, dueDate = dueDate.orToday())
    } else {
        copy(hasReminder = false)
    }

    /**
     * [Recurrence.None] leaves [dueDate] to be picked manually; any other recurrence auto-fills it
     * (today if empty, kept as-is otherwise) since a recurring item is never edited to a specific date.
     */
    fun changeRecurrence(recurrence: Recurrence): AddItemUiState = copy(
        recurrence = recurrence,
        dueDate = if (recurrence == Recurrence.None) dueDate else dueDate.orToday(),
    )

    /** Clearing [dueDate] also clears [dueTime]/[recurrence]/[reminderWarning] — none of them mean
     * anything without a date. */
    fun changeDueDate(dueDate: LocalDate?): AddItemUiState = copy(
        dueDate = dueDate,
        dueTime = if (dueDate == null) null else dueTime,
        recurrence = if (dueDate == null) Recurrence.None else recurrence,
        reminderWarning = if (dueDate == null) ReminderWarning.None else reminderWarning,
    )

    fun reduce(event: AddItemEvent.FieldEvent): AddItemUiState = when (event) {
        is AddItemEvent.OnTypeChanged -> changeType(event.type)
        is AddItemEvent.OnTitleChanged -> changeTitle(event.title)
        is AddItemEvent.OnDescriptionChanged -> changeDescription(event.description)
        is AddItemEvent.OnSectionChanged -> setSection(event.sectionId)
        is AddItemEvent.OnTagToggled -> setTag(event.tagId)
        is AddItemEvent.OnReminderToggled -> toggleReminder(event.enabled)
        is AddItemEvent.OnDueDateChanged -> changeDueDate(event.dueDate)
        is AddItemEvent.OnDueTimeChanged -> copy(dueTime = event.dueTime)
        is AddItemEvent.OnRecurrenceChanged -> changeRecurrence(event.recurrence)
        is AddItemEvent.OnReminderWarningChanged -> copy(reminderWarning = event.reminderWarning)
    }
}
