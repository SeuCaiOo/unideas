package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
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

class ItemOccurrenceViewModel(
    private val itemId: Long?,
    private val itemFormUseCase: ItemFormUseCase,
    private val itemOccurrenceUseCase: ItemOccurrenceUseCase,
) : ViewModel() {

    private var originalItem: Item? = null
    private var historyJob: Job? = null

    private val _uiState = MutableStateFlow(ItemOccurrenceUiState())
    val uiState: StateFlow<ItemOccurrenceUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ItemOccurrenceUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemOccurrenceUiAction> = _uiAction.receiveAsFlow()

    private val _dialogState =
        MutableStateFlow<ItemOccurrenceDialogState>(ItemOccurrenceDialogState.None)
    val dialogState: StateFlow<ItemOccurrenceDialogState> = _dialogState.asStateFlow()

    private val _historyState = MutableStateFlow<List<ItemCompletionHistory>>(emptyList())
    val historyState: StateFlow<List<ItemCompletionHistory>> = _historyState.asStateFlow()

    init {
        val id = itemId
        if (id != null) {
            viewModelScope.launch {
                val item = itemFormUseCase.get(id).first() ?: return@launch
                originalItem = item
                _uiState.update {
                    it.copy(
                        isCompleted = item.isCompleted,
                        completedAt = item.completedAt
                    )
                }
            }
        }
    }

    fun onEvent(event: ItemOccurrenceEvent) {
        when (event) {
            is ItemOccurrenceEvent.OnCompleteClicked -> handleCompleteClicked()
            is ItemOccurrenceEvent.OnCompleteConfirmClicked -> {
                _dialogState.update { ItemOccurrenceDialogState.None }
                handleComplete()
            }

            is ItemOccurrenceEvent.OnHistoryClicked -> handleHistoryClicked()
            is ItemOccurrenceEvent.OnDialogDismissed -> {
                historyJob?.cancel()
                _dialogState.update { ItemOccurrenceDialogState.None }
            }
        }
    }

    private fun handleCompleteClicked() {
        if (uiState.value.isCompleted) {
            _dialogState.update { ItemOccurrenceDialogState.ReopenConfirm }
        } else {
            handleComplete()
        }
    }

    private fun handleComplete() = viewModelScope.launch {
        val item = originalItem ?: return@launch
        if (item.type != ItemType.TASK) return@launch
        val now = LocalDateTime.now()
        itemOccurrenceUseCase.complete(item, now)
            .onSuccess { result ->
                val updated = itemFormUseCase.get(item.id).first() ?: return@onSuccess
                originalItem = updated
                _uiState.update {
                    it.copy(
                        isCompleted = updated.isCompleted,
                        completedAt = updated.completedAt
                    )
                }
                if (result == CompletionResult.Completed) {
                    sendUiAction(ItemOccurrenceUiAction.ShowSnackbar(R.string.item_detail_completed_snackbar))
                }
            }
            .onFailure { sendUiAction(ItemOccurrenceUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleHistoryClicked() {
        val id = itemId ?: return
        _dialogState.update { ItemOccurrenceDialogState.History }
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            itemOccurrenceUseCase.getHistory(id)
                .collect { history -> _historyState.update { history } }
        }
    }

    private suspend fun sendUiAction(action: ItemOccurrenceUiAction) = _uiAction.send(action)
}
