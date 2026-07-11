package com.seucaio.unideas.feature.items.features.form.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.domain.usecase.item.EditItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel for the single create/edit Item form. Unlike Sections/Tags, this screen has real
 * UI-only state (the fields being typed), so `uiState` keeps deriving from `InternalState`
 * instead of the "flat list" exception — but no `combine` is needed for it: neither the
 * available sections nor the available tags can change while this screen is open (both are
 * only created/edited from Settings, a separate screen), so [GetSectionsAndTagsUseCase] is a
 * one-time snapshot loaded once, same as [originalItem].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemFormViewModel(
    private val itemId: Long?,
    private val getItem: GetItemUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
    private val createItem: CreateItemUseCase,
    private val editItem: EditItemUseCase,
) : ViewModel() {

    private data class InternalState(
        val type: ItemType = ItemType.TASK,
        val title: String = "",
        val description: String = "",
        val sectionId: Long? = null,
        val selectedTagIds: Set<Long> = emptySet(),
        val dueDate: LocalDate? = null,
        val recurrence: Recurrence = Recurrence.None,
        val availableSections: List<Section> = emptyList(),
        val availableTags: List<Tag> = emptyList(),
    )

    // Preserved once an existing item loads, so save() can carry over id/createdAt/completedAt
    // without the editable fields needing to track them.
    private var originalItem: Item? = null

    private val internalState = MutableStateFlow(InternalState())
    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<ItemFormUiState> = retryTrigger.flatMapLatest {
        internalState
            .map<InternalState, ItemFormUiState> { internal ->
                ItemFormUiState.Success(
                    isEditing = itemId != null,
                    type = internal.type,
                    title = internal.title,
                    description = internal.description,
                    sectionId = internal.sectionId,
                    selectedTagIds = internal.selectedTagIds,
                    dueDate = internal.dueDate,
                    recurrence = internal.recurrence,
                    availableSections = internal.availableSections,
                    availableTags = internal.availableTags,
                )
            }
            .onStart { loadFormData() }
            .catch { emit(ItemFormUiState.Error(R.string.item_form_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), ItemFormUiState.Loading)

    private val _uiAction = Channel<ItemFormUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemFormUiAction> = _uiAction.receiveAsFlow()

    private suspend fun loadFormData() {
        val referenceData = getSectionsAndTags()
        internalState.update {
            it.copy(availableSections = referenceData.sections, availableTags = referenceData.tags)
        }
        if (itemId != null) loadItem(itemId)
    }

    private suspend fun loadItem(id: Long) {
        val item = getItem(id).first() ?: error("Item $id not found")
        originalItem = item
        internalState.update {
            it.copy(
                type = item.type,
                title = item.title,
                description = item.description.orEmpty(),
                sectionId = item.sectionId,
                selectedTagIds = item.tags.map { tag -> tag.id }.toSet(),
                dueDate = item.dueDate,
                recurrence = item.recurrence,
            )
        }
    }

    fun onEvent(event: ItemFormEvent) {
        when (event) {
            is ItemFormEvent.OnTypeChanged -> internalState.update { it.copy(type = event.type) }
            is ItemFormEvent.OnTitleChanged -> internalState.update { it.copy(title = event.title) }
            is ItemFormEvent.OnDescriptionChanged ->
                internalState.update { it.copy(description = event.description) }
            is ItemFormEvent.OnSectionChanged -> internalState.update { it.copy(sectionId = event.sectionId) }
            is ItemFormEvent.OnTagToggled -> internalState.update { it.toggleTag(event.tagId) }
            is ItemFormEvent.OnDueDateChanged -> internalState.update {
                it.copy(
                    dueDate = event.dueDate,
                    recurrence = if (event.dueDate == null) Recurrence.None else it.recurrence,
                )
            }
            is ItemFormEvent.OnRecurrenceChanged -> internalState.update { it.copy(recurrence = event.recurrence) }
            is ItemFormEvent.OnSaveClicked -> handleSave()
            is ItemFormEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun InternalState.toggleTag(tagId: Long): InternalState =
        copy(selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId)

    private fun handleSave() = viewModelScope.launch {
        val state = internalState.value
        val selectedTags = state.availableTags.filter { it.id in state.selectedTagIds }

        val result: Result<Unit> = if (itemId == null) {
            createItem(
                Item(
                    type = state.type,
                    title = state.title,
                    description = state.description.ifBlank { null },
                    sectionId = state.sectionId,
                    dueDate = state.dueDate,
                    recurrence = state.recurrence,
                    createdAt = LocalDateTime.now(),
                    tags = selectedTags,
                ),
            ).map { }
        } else {
            val original = originalItem ?: return@launch
            editItem(
                original.copy(
                    type = state.type,
                    title = state.title,
                    description = state.description.ifBlank { null },
                    sectionId = state.sectionId,
                    dueDate = state.dueDate,
                    recurrence = state.recurrence,
                    tags = selectedTags,
                ),
            )
        }

        result.onSuccess { sendUiAction(ItemFormUiAction.NavigateBack) }.onFailure { handleFailure(it) }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            sendUiAction(ItemFormUiAction.ShowSnackbar(R.string.item_title_required))
        } else {
            sendUiAction(ItemFormUiAction.ShowError(error.message.orEmpty()))
        }
    }

    private suspend fun sendUiAction(action: ItemFormUiAction) = _uiAction.send(action)
}
