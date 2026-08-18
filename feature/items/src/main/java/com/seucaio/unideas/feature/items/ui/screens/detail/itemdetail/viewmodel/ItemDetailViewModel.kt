package com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ItemDetailViewModel(
    private val itemId: Long?,
    private val itemFormUseCase: ItemFormUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
    private val savedStateHandle: SavedStateHandle,
    initialType: ItemType = ItemType.TASK,
) : ViewModel() {

    private companion object {
        const val TEXT_DEBOUNCE_MS = 500L
        const val UI_STATE_KEY = "itemDetailUiState"
    }

    private var originalItem: Item? = null
    private var currentItemId: Long? = itemId
    private var debounceJob: Job? = null
    private var hasPendingTextSave = false

    private val editUiState = MutableStateFlow(
        ItemDetailUiState(
            itemId = itemId,
            isLoading = itemId != null,
            type = initialType
        )
    )

    val uiState: StateFlow<ItemDetailUiState> = if (itemId == null) {
        savedStateHandle.getStateFlow(UI_STATE_KEY, ItemDetailUiState(type = initialType))
    } else {
        editUiState.asStateFlow()
    }

    private fun updateUiState(transform: (ItemDetailUiState) -> ItemDetailUiState) {
        if (itemId == null) {
            savedStateHandle[UI_STATE_KEY] = transform(uiState.value)
        } else {
            editUiState.update(transform)
        }
    }

    private val _uiAction = Channel<ItemDetailUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemDetailUiAction> = _uiAction.receiveAsFlow()

    private val _dialogState = MutableStateFlow<ItemDetailDialogState>(ItemDetailDialogState.None)
    val dialogState: StateFlow<ItemDetailDialogState> = _dialogState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
                updateUiState { it.setReferenceData(referenceData.sections, referenceData.tags) }
            }
            if (itemId != null) loadItem(itemId)
        }
    }

    private suspend fun loadItem(id: Long) {
        val item = runCatching { itemFormUseCase.get(id).first() }.getOrNull()
        if (item == null) {
            sendUiAction(ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error))
            updateUiState { it.markLoadFailed() }
            return
        }
        originalItem = item
        updateUiState { it.applyLoadedItem(item) }
    }

    fun onEvent(event: ItemDetailEvent) {
        when (event) {
            is ItemDetailEvent.FieldEvent -> handleFieldEvent(event)
            is ItemDetailEvent.OnShareClicked -> handleShare()
            is ItemDetailEvent.OnDeleteClicked -> _dialogState.update { ItemDetailDialogState.DeleteConfirm }
            is ItemDetailEvent.OnDialogDismissed -> _dialogState.update { ItemDetailDialogState.None }
            is ItemDetailEvent.OnDeleteConfirmClicked -> handleDelete()
            is ItemDetailEvent.OnRetryClicked -> retryLoad()
            is ItemDetailEvent.OnBackRequested -> handleBackRequested()
            is ItemDetailEvent.OnDiscardConfirmed -> handleDiscardConfirmed()
            is ItemDetailEvent.OnItemUpdatedExternally -> handleItemUpdatedExternally(event.item)
            is ItemDetailEvent.OnScreenResumed -> itemId?.let { id -> viewModelScope.launch { loadItem(id) } }
        }
    }

    private fun handleItemUpdatedExternally(item: Item) {
        originalItem = originalItem?.copy(
            completedAt = item.completedAt,
            lastCompletedScheduledDate = item.lastCompletedScheduledDate,
            dueDate = item.dueDate,
            dueTime = item.dueTime,
            recurrence = item.recurrence,
            reminderWarning = item.reminderWarning,
        )
        updateUiState { it.applyExternalOccurrenceUpdate(item) }
    }

    private fun retryLoad() {
        val id = itemId ?: return
        updateUiState { it.startLoading() }
        viewModelScope.launch { loadItem(id) }
    }

    private fun handleFieldEvent(event: ItemDetailEvent.FieldEvent) {
        updateUiState { it.reduce(event) }
        debounceJob?.cancel()
        hasPendingTextSave = true
        debounceJob = viewModelScope.launch {
            delay(TEXT_DEBOUNCE_MS)
            hasPendingTextSave = false
            if (uiState.value.isTitleValid) persist().onFailure { handleFailure(it) }
        }
    }

    /** Safety save: a pending text debounce shouldn't be lost just because the user left the
     * screen before it fired. [viewModelScope] is already cancelled by the time this runs, so the
     * flush can't ride on it — a short, local Room write is an acceptable synchronous cost here. */
    override fun onCleared() {
        if (hasPendingTextSave && uiState.value.isTitleValid) {
            runBlocking { persist() }
        }
        super.onCleared()
    }

    private fun handleBackRequested() = viewModelScope.launch {
        val state = uiState.value
        when {
            state.isPristine -> sendUiAction(ItemDetailUiAction.NavigateBack)
            state.isTitleValid -> {
                debounceJob?.cancel()
                hasPendingTextSave = false
                persist().onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }.onFailure { handleFailure(it) }
            }

            state.titleError -> _dialogState.update { discardConfirmDialogState() }

            else -> updateUiState { it.copy(titleError = true) }
        }
    }

    private fun discardConfirmDialogState(): ItemDetailDialogState.DiscardConfirm = if (itemId != null) {
        ItemDetailDialogState.DiscardConfirm(
            titleRes = R.string.item_detail_discard_edit_title,
            messageRes = R.string.item_detail_discard_edit_message,
        )
    } else {
        ItemDetailDialogState.DiscardConfirm(
            titleRes = R.string.item_detail_discard_new_title,
            messageRes = R.string.item_detail_discard_new_message,
        )
    }

    private fun handleDiscardConfirmed() = viewModelScope.launch {
        _dialogState.update { ItemDetailDialogState.None }
        val id = currentItemId
        if (itemId == null && id != null) {
            itemFormUseCase.delete(id)
                .onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }
                .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
        } else {
            sendUiAction(ItemDetailUiAction.NavigateBack)
        }
    }

    private suspend fun persist(): Result<Unit> {
        val id = currentItemId

        return if (id == null) {
            val newItem = uiState.value.toItem(original = null)
            itemFormUseCase.create(newItem)
                .onSuccess { newId ->
                    currentItemId = newId
                    val createdItem = newItem.copy(id = newId)
                    originalItem = createdItem
                    updateUiState { it.copy(itemId = newId) }
                    sendUiAction(ItemDetailUiAction.ItemPersisted(createdItem))
                }
                .map { }
        } else {
            val original = originalItem ?: return Result.failure(IllegalStateException("Item not loaded"))
            val updated = uiState.value.toItem(original)
            itemFormUseCase.edit(updated).onSuccess {
                originalItem = updated
                sendUiAction(ItemDetailUiAction.ItemPersisted(updated))
            }
        }
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
        itemFormUseCase.delete(id)
            .onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }
            .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleShare() = viewModelScope.launch {
        val item = originalItem ?: return@launch
        sendUiAction(ItemDetailUiAction.ShareText(item))
    }

    private suspend fun sendUiAction(action: ItemDetailUiAction) = _uiAction.send(action)
}
