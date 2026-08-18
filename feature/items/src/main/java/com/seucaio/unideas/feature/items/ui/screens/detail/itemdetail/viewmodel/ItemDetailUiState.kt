package com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueDate
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueTime
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableRecurrence
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableReminderWarning
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ItemDetailUiState(
    val itemId: Long? = null,
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
    val loadFailed: Boolean = false,
    override val titleError: Boolean = false,
) : ItemFormFieldsState, Serializable {

    override val isEditing: Boolean get() = itemId != null

    override val isTitleValid: Boolean get() = title.isNotBlank()

    override val typeIsTask: Boolean get() = type == ItemType.TASK

    val isPristine: Boolean
        get() = title.isBlank() && description.isBlank() && sectionId == null &&
            selectedTagIds.isEmpty() && dueDate == null

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
        loadFailed = false,
    )

    fun applyExternalOccurrenceUpdate(item: Item): ItemDetailUiState = copy(
        hasReminder = item.dueDate != null,
        dueDate = item.dueDate,
        dueTime = item.dueTime,
        recurrence = item.recurrence,
        reminderWarning = item.reminderWarning,
    )

    /** Applies the current fields onto [original] — `null` (first save while creating) starts from a
     * blank [Item] instead. */
    fun toItem(original: Item?): Item =
        (original ?: Item(type = type, title = title, createdAt = LocalDateTime.now()))
            .copy(
                type = type,
                title = title,
                description = description.ifBlank { null },
                sectionId = sectionId,
                dueDate = persistableDueDate,
                dueTime = persistableDueTime,
                recurrence = persistableRecurrence,
                reminderWarning = persistableReminderWarning,
                tags = availableTags.filter { it.id in selectedTagIds },
            )

    fun reduce(event: ItemDetailEvent.FieldEvent): ItemDetailUiState = when (event) {
        is ItemDetailEvent.OnTitleChanged -> copy(title = event.title, titleError = false)
        is ItemDetailEvent.OnDescriptionChanged -> copy(description = event.description)
    }
}
