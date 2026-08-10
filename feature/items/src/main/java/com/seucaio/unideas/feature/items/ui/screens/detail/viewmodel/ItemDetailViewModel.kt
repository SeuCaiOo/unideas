package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.Job
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
    private var currentItemId: Long? = itemId
    private var historyJob: Job? = null

    private val _uiState = MutableStateFlow(
        ItemDetailUiState(isEditing = itemId != null, isLoading = itemId != null, type = initialType),
    )
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ItemDetailUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemDetailUiAction> = _uiAction.receiveAsFlow()

    private val _dialogState = MutableStateFlow<ItemDetailDialogState>(ItemDetailDialogState.None)
    val dialogState: StateFlow<ItemDetailDialogState> = _dialogState.asStateFlow()

    private val _historyState = MutableStateFlow<List<ItemCompletionHistory>>(emptyList())
    val historyState: StateFlow<List<ItemCompletionHistory>> = _historyState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
                _uiState.update { it.setReferenceData(referenceData.sections, referenceData.tags) }
            }
            if (itemId != null) loadItem(itemId)
        }
    }

    private suspend fun loadItem(id: Long) {
        val item = runCatching { itemFormUseCase.get(id).first() }.getOrNull()
        if (item == null) {
            sendUiAction(ItemDetailUiAction.ShowSnackbar(R.string.item_form_load_error))
            _uiState.update { it.markLoadFailed() }
            return
        }
        originalItem = item
        _uiState.update { it.applyLoadedItem(item) }
    }

    fun onEvent(event: ItemDetailEvent) {
        when (event) {
            is ItemDetailEvent.FieldEvent -> handleFieldEvent(event)
            is ItemDetailEvent.OnSaveClicked -> handleSaveClicked()
            is ItemDetailEvent.OnShareClicked -> handleShare()
            is ItemDetailEvent.OnDeleteClicked -> _dialogState.update { ItemDetailDialogState.DeleteConfirm }
            is ItemDetailEvent.OnDialogDismissed -> {
                historyJob?.cancel()
                _dialogState.update { ItemDetailDialogState.None }
            }
            is ItemDetailEvent.OnDeleteConfirmClicked -> handleDelete()
            is ItemDetailEvent.OnCompleteClicked -> handleCompleteClicked()
            is ItemDetailEvent.OnCompleteConfirmClicked -> {
                _dialogState.update { ItemDetailDialogState.None }
                handleComplete()
            }
            is ItemDetailEvent.OnHistoryClicked -> handleHistoryClicked()
            is ItemDetailEvent.OnRetryClicked -> retryLoad()
        }
    }

    private fun retryLoad() {
        val id = itemId ?: return
        _uiState.update { it.startLoading() }
        viewModelScope.launch { loadItem(id) }
    }

    private fun handleFieldEvent(event: ItemDetailEvent.FieldEvent) {
        _uiState.update { it.reduce(event) }
        val shouldAutoSave = event !is ItemDetailEvent.OnTitleChanged &&
            event !is ItemDetailEvent.OnDescriptionChanged &&
            _uiState.value.isTitleValid
        if (shouldAutoSave) {
            viewModelScope.launch { persist().onFailure { handleFailure(it) } }
        }
    }

    private fun handleSaveClicked() = viewModelScope.launch {
        if (!_uiState.value.isTitleValid) {
            sendUiAction(ItemDetailUiAction.ShowSnackbar(R.string.item_title_required))
            return@launch
        }
        persist().onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }.onFailure { handleFailure(it) }
    }

    private suspend fun persist(): Result<Unit> {
        val id = currentItemId

        return if (id == null) {
            val newItem = _uiState.value.toItem(original = null)
            itemFormUseCase.create(newItem)
                .onSuccess { newId ->
                    currentItemId = newId
                    originalItem = newItem.copy(id = newId)
                }
                .map { }
        } else {
            val original = originalItem ?: return Result.failure(IllegalStateException("Item not loaded"))
            val updated = _uiState.value.toItem(original)
            itemFormUseCase.edit(updated).onSuccess { originalItem = updated }
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

    private fun handleCompleteClicked() {
        if (_uiState.value.isCompleted) {
            _dialogState.update { ItemDetailDialogState.ReopenConfirm }
        } else {
            handleComplete()
        }
    }

    private fun handleComplete() = viewModelScope.launch {
        val item = originalItem ?: return@launch
        if (item.type != ItemType.TASK) return@launch
        val now = LocalDateTime.now()
        itemFormUseCase.complete(item, now)
            .onSuccess {
                val updated = itemFormUseCase.get(item.id).first() ?: return@onSuccess
                originalItem = updated
                _uiState.update { it.applyCompletion(updated) }
            }
            .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleHistoryClicked() {
        val id = itemId ?: return
        _dialogState.update { ItemDetailDialogState.History }
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            itemFormUseCase.getHistory(id).collect { history -> _historyState.update { history } }
        }
    }

    private fun handleShare() = viewModelScope.launch {
        val item = originalItem ?: return@launch
        sendUiAction(ItemDetailUiAction.ShareText(item))
    }

    private suspend fun sendUiAction(action: ItemDetailUiAction) = _uiAction.send(action)
}
