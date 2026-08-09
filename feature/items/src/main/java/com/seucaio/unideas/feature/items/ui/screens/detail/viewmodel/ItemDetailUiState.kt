package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import com.seucaio.unideas.core.common.extensions.orToday
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ItemDetailUiState(
    override val isEditing: Boolean = false,
    val isLoading: Boolean = false,
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
    override val isCompleted: Boolean = false,
    override val completedAt: LocalDateTime? = null,
    val loadFailed: Boolean = false,
) : ItemFormFieldsState {

    override val isTitleValid: Boolean get() = title.isNotBlank()

    override val typeIsTask: Boolean get() = type == ItemType.TASK

    fun toggleReminder(enabled: Boolean): ItemDetailUiState = if (enabled) {
        copy(hasReminder = true, dueDate = dueDate.orToday())
    } else {
        copy(hasReminder = false)
    }

    /**
     * [Recurrence.None] leaves [dueDate] to be picked manually; any other recurrence auto-fills it
     * (today if empty, kept as-is otherwise) since a recurring item is never edited to a specific date.
     */
    fun changeRecurrence(recurrence: Recurrence): ItemDetailUiState = copy(
        recurrence = recurrence,
        dueDate = if (recurrence == Recurrence.None) dueDate else dueDate.orToday(),
    )

    fun toggleTag(tagId: Long): ItemDetailUiState =
        copy(selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId)

    fun setReferenceData(sections: List<Section>, tags: List<Tag>): ItemDetailUiState =
        copy(availableSections = sections, availableTags = tags)

    fun startLoading(): ItemDetailUiState = copy(isLoading = true, loadFailed = false)

    fun markLoadFailed(): ItemDetailUiState = copy(isLoading = false, loadFailed = true)

    fun applyLoadedItem(item: Item): ItemDetailUiState = copy(
        isLoading = false,
        type = item.type,
        title = item.title,
        description = item.description.orEmpty(),
        sectionId = item.sectionId,
        selectedTagIds = item.tags.map { it.id }.toSet(),
        hasReminder = item.dueDate != null,
        dueDate = item.dueDate,
        dueTime = item.dueTime,
        recurrence = item.recurrence,
        reminderWarning = item.reminderWarning,
        isCompleted = item.isCompleted,
        completedAt = item.completedAt,
        loadFailed = false,
    )

    fun applyCompletion(item: Item): ItemDetailUiState =
        copy(isCompleted = item.isCompleted, completedAt = item.completedAt, dueDate = item.dueDate)

    /** Clearing [dueDate] also clears [dueTime]/[recurrence]/[reminderWarning] — none of them mean
     * anything without a date. */
    fun changeDueDate(dueDate: LocalDate?): ItemDetailUiState = copy(
        dueDate = dueDate,
        dueTime = if (dueDate == null) null else dueTime,
        recurrence = if (dueDate == null) Recurrence.None else recurrence,
        reminderWarning = if (dueDate == null) ReminderWarning.None else reminderWarning,
    )

    fun reduce(event: ItemDetailEvent.FieldEvent): ItemDetailUiState = when (event) {
        is ItemDetailEvent.OnTypeChanged -> copy(type = event.type)
        is ItemDetailEvent.OnTitleChanged -> copy(title = event.title)
        is ItemDetailEvent.OnDescriptionChanged -> copy(description = event.description)
        is ItemDetailEvent.OnSectionChanged -> copy(sectionId = event.sectionId)
        is ItemDetailEvent.OnTagToggled -> toggleTag(event.tagId)
        is ItemDetailEvent.OnReminderToggled -> toggleReminder(event.enabled)
        is ItemDetailEvent.OnDueDateChanged -> changeDueDate(event.dueDate)
        is ItemDetailEvent.OnDueTimeChanged -> copy(dueTime = event.dueTime)
        is ItemDetailEvent.OnRecurrenceChanged -> changeRecurrence(event.recurrence)
        is ItemDetailEvent.OnReminderWarningChanged -> copy(reminderWarning = event.reminderWarning)
    }
}
