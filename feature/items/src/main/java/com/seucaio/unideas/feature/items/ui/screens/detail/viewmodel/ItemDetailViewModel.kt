package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
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

class ItemDetailViewModel(
    private val itemId: Long?,
    private val itemFormUseCase: ItemFormUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
    initialType: ItemType = ItemType.TASK,
) : ViewModel() {

    private var originalItem: Item? = null

    private val _uiState = MutableStateFlow(
        ItemDetailUiState(isEditing = itemId != null, isLoading = itemId != null, type = initialType),
    )
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ItemDetailUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemDetailUiAction> = _uiAction.receiveAsFlow()

    private val _dialogState = MutableStateFlow<ItemDetailDialogState>(ItemDetailDialogState.None)
    val dialogState: StateFlow<ItemDetailDialogState> = _dialogState.asStateFlow()

    init {
        viewModelScope.launch { loadFormData() }
    }

    private suspend fun loadFormData() {
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
            sendUiAction(ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error))
            _uiState.update { it.copy(isLoading = false, loadFailed = true) }
            return
        }
        originalItem = item
        _uiState.update {
            it.copy(
                isLoading = false,
                type = item.type,
                title = item.title,
                description = item.description.orEmpty(),
                sectionId = item.sectionId,
                selectedTagIds = item.tags.map { tag -> tag.id }.toSet(),
                dueDate = item.dueDate,
                recurrence = item.recurrence,
                isCompleted = item.isCompleted,
                loadFailed = false,
            )
        }
    }

    fun onEvent(event: ItemDetailEvent) {
        when (event) {
            is ItemDetailEvent.OnTypeChanged -> _uiState.update { it.copy(type = event.type) }
            is ItemDetailEvent.OnTitleChanged -> _uiState.update { it.copy(title = event.title) }
            is ItemDetailEvent.OnDescriptionChanged ->
                _uiState.update { it.copy(description = event.description) }
            is ItemDetailEvent.OnSectionChanged -> _uiState.update { it.copy(sectionId = event.sectionId) }
            is ItemDetailEvent.OnTagToggled -> _uiState.update { it.toggleTag(event.tagId) }
            is ItemDetailEvent.OnDueDateChanged -> _uiState.update {
                it.copy(
                    dueDate = event.dueDate,
                    recurrence = if (event.dueDate == null) Recurrence.None else it.recurrence,
                )
            }
            is ItemDetailEvent.OnRecurrenceChanged -> _uiState.update { it.copy(recurrence = event.recurrence) }
            is ItemDetailEvent.OnSaveClicked -> handleSave()
            is ItemDetailEvent.OnShareClicked -> handleShare()
            is ItemDetailEvent.OnDeleteClicked -> _dialogState.update { ItemDetailDialogState.DeleteConfirm }
            is ItemDetailEvent.OnDialogDismissed -> _dialogState.update { ItemDetailDialogState.None }
            is ItemDetailEvent.OnDeleteConfirmClicked -> handleDelete()
            is ItemDetailEvent.OnCompleteClicked -> handleComplete()
            is ItemDetailEvent.OnRetryClicked -> retryLoad()
        }
    }

    private fun retryLoad() {
        val id = itemId ?: return
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch { loadItem(id) }
    }

    private fun ItemDetailUiState.toggleTag(tagId: Long): ItemDetailUiState =
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

        result.onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }.onFailure { handleFailure(it) }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            sendUiAction(ItemDetailUiAction.ShowSnackbar(R.string.item_title_required))
        } else {
            sendUiAction(ItemDetailUiAction.ShowError(error.message.orEmpty()))
        }
    }

    private fun handleDelete() = viewModelScope.launch {
        val id = itemId ?: return@launch
        _dialogState.update { ItemDetailDialogState.None }
        runCatching { itemFormUseCase.delete(id) }
            .onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }
            .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleComplete() = viewModelScope.launch {
        val item = originalItem ?: return@launch
        if (item.type != ItemType.TASK) return@launch
        itemFormUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleShare() = viewModelScope.launch {
        val item = originalItem ?: return@launch
        sendUiAction(ItemDetailUiAction.ShareText(buildShareText(item)))
    }

    private fun buildShareText(item: Item): String = buildString {
        appendLine(item.title)
        item.description?.let { appendLine(it) }
        item.dueDate?.let { appendLine(it.toFormattedDateString()) }
    }

    private suspend fun sendUiAction(action: ItemDetailUiAction) = _uiAction.send(action)
}
