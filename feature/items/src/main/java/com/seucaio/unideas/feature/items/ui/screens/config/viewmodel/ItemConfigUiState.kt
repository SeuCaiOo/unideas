package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel

import com.seucaio.unideas.core.common.extensions.orToday
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate
import java.time.LocalTime

data class ItemConfigUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val type: ItemType = ItemType.TASK,
    val sectionId: Long? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val hasReminder: Boolean = false,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val recurrence: Recurrence = Recurrence.None,
    val reminderWarning: ReminderWarning = ReminderWarning.None,
    val availableSections: List<Section> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
) {

    val persistableDueDate: LocalDate? get() = if (hasReminder) dueDate else null

    val persistableDueTime: LocalTime? get() = if (hasReminder) dueTime else null

    val persistableRecurrence: Recurrence get() = if (hasReminder) recurrence else Recurrence.None

    val persistableReminderWarning: ReminderWarning
        get() = if (hasReminder) reminderWarning else ReminderWarning.None

    fun toggleReminder(enabled: Boolean): ItemConfigUiState = copy(hasReminder = enabled)

    fun toggleTag(tagId: Long): ItemConfigUiState =
        copy(selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId)

    fun changeRecurrence(recurrence: Recurrence): ItemConfigUiState = copy(
        recurrence = recurrence,
        dueDate = if (recurrence == Recurrence.None) dueDate else dueDate.orToday(),
    )

    fun changeDueDate(dueDate: LocalDate?): ItemConfigUiState = copy(
        dueDate = dueDate,
        dueTime = if (dueDate == null) null else dueTime,
        recurrence = if (dueDate == null) Recurrence.None else recurrence,
        reminderWarning = if (dueDate == null) ReminderWarning.None else reminderWarning,
    )

    fun applyLoadedItem(item: Item): ItemConfigUiState = copy(
        isLoading = false,
        loadFailed = false,
        type = item.type,
        sectionId = item.sectionId,
        selectedTagIds = item.tags.map { it.id }.toSet(),
        hasReminder = item.dueDate != null,
        dueDate = item.dueDate,
        dueTime = item.dueTime,
        recurrence = item.recurrence,
        reminderWarning = item.reminderWarning,
    )

    fun setReferenceData(sections: List<Section>, tags: List<Tag>): ItemConfigUiState =
        copy(availableSections = sections, availableTags = tags)

    fun startLoading(): ItemConfigUiState = copy(isLoading = true, loadFailed = false)

    fun markLoadFailed(): ItemConfigUiState = copy(isLoading = false, loadFailed = true)

    fun toItem(original: Item): Item = original.copy(
        sectionId = sectionId,
        dueDate = persistableDueDate,
        dueTime = persistableDueTime,
        recurrence = persistableRecurrence,
        reminderWarning = persistableReminderWarning,
        tags = availableTags.filter { it.id in selectedTagIds },
    )

    fun switchedType(original: Item, newType: ItemType): Item = original.copy(
        type = newType,
        dueDate = null,
        dueTime = null,
        recurrence = Recurrence.None,
        reminderWarning = ReminderWarning.None,
    )

    fun afterTypeSwitch(newType: ItemType): ItemConfigUiState = copy(
        type = newType,
        hasReminder = false,
        dueDate = null,
        dueTime = null,
        recurrence = Recurrence.None,
        reminderWarning = ReminderWarning.None,
    )
}
