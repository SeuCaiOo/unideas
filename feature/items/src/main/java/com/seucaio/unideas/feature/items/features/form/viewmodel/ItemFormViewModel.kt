package com.seucaio.unideas.feature.items.features.form.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel for the single create/edit Item form. Unlike Sections/Tags/the detail screen, this
 * uiState is **not** derived from a reactive domain [kotlinx.coroutines.flow.Flow] — there is
 * nothing here to keep observing after the initial load: sections/tags can't change while this
 * screen is open (only Settings creates/edits them), and the item being edited isn't mutated by
 * anything else while the user is typing. So `uiState` is a plain [MutableStateFlow] mutated
 * directly by `onEvent`/the one-shot load in `init`, not a mapped/combined [Flow] — no
 * `Loading`/`Error` screen state exists (see [ItemFormUiState]'s doc), so there's nothing to
 * retry either.
 */
class ItemFormViewModel(
    private val itemId: Long?,
    private val itemFormUseCase: ItemFormUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
) : ViewModel() {

    // Preserved once an existing item loads, so save() can carry over id/createdAt/completedAt
    // without the editable fields needing to track them.
    private var originalItem: Item? = null

    private val _uiState = MutableStateFlow(ItemFormUiState(isEditing = itemId != null))
    val uiState: StateFlow<ItemFormUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ItemFormUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemFormUiAction> = _uiAction.receiveAsFlow()

    init {
        viewModelScope.launch { loadFormData() }
    }

    private suspend fun loadFormData() {
        // Failure here (rare — GetSectionsAndTagsUseCase already falls back to empty lists on
        // its own) just leaves availableSections/availableTags empty, same as Sections/Tags
        // screens showing an empty list — not a screen-level error, nothing to tell the user.
        runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
            _uiState.update {
                it.copy(availableSections = referenceData.sections, availableTags = referenceData.tags)
            }
        }
        if (itemId != null) loadItem(itemId)
    }

    private suspend fun loadItem(id: Long) {
        val item = runCatching { itemFormUseCase.get(id).first() }.getOrNull()
        if (item == null) {
            sendUiAction(ItemFormUiAction.ShowSnackbar(R.string.item_form_load_error))
            sendUiAction(ItemFormUiAction.NavigateBack)
            return
        }
        originalItem = item
        _uiState.update {
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
            is ItemFormEvent.OnTypeChanged -> _uiState.update { it.copy(type = event.type) }
            is ItemFormEvent.OnTitleChanged -> _uiState.update { it.copy(title = event.title) }
            is ItemFormEvent.OnDescriptionChanged ->
                _uiState.update { it.copy(description = event.description) }
            is ItemFormEvent.OnSectionChanged -> _uiState.update { it.copy(sectionId = event.sectionId) }
            is ItemFormEvent.OnTagToggled -> _uiState.update { it.toggleTag(event.tagId) }
            is ItemFormEvent.OnDueDateChanged -> _uiState.update {
                it.copy(
                    dueDate = event.dueDate,
                    recurrence = if (event.dueDate == null) Recurrence.None else it.recurrence,
                )
            }
            is ItemFormEvent.OnRecurrenceChanged -> _uiState.update { it.copy(recurrence = event.recurrence) }
            is ItemFormEvent.OnSaveClicked -> handleSave()
        }
    }

    private fun ItemFormUiState.toggleTag(tagId: Long): ItemFormUiState =
        copy(selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId else selectedTagIds + tagId)

    private fun handleSave() = viewModelScope.launch {
        val state = _uiState.value
        val selectedTags = state.availableTags.filter { it.id in state.selectedTagIds }

        val result: Result<Unit> = if (itemId == null) {
            itemFormUseCase.create(
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
            itemFormUseCase.edit(
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
