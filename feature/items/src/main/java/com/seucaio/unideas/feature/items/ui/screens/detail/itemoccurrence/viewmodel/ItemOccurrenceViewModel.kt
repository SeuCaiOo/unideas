package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
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
import java.time.LocalDate
import java.time.LocalDateTime

class ItemOccurrenceViewModel(
    private val itemId: Long?,
    private val itemFormUseCase: ItemFormUseCase,
    private val itemOccurrenceUseCase: ItemOccurrenceUseCase,
) : ViewModel() {

    private var originalItem: Item? = null

    private val _uiState = MutableStateFlow(ItemOccurrenceUiState())
    val uiState: StateFlow<ItemOccurrenceUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ItemOccurrenceUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemOccurrenceUiAction> = _uiAction.receiveAsFlow()

    private val _dialogState =
        MutableStateFlow<ItemOccurrenceDialogState>(ItemOccurrenceDialogState.None)
    val dialogState: StateFlow<ItemOccurrenceDialogState> = _dialogState.asStateFlow()

    init {
        val id = itemId
        if (id != null) {
            viewModelScope.launch {
                val item = itemFormUseCase.get(id).first() ?: return@launch
                originalItem = item
                _uiState.update {
                    it.copy(
                        isCompleted = item.isCompleted,
                        completedAt = item.completedAt,
                        dueDate = item.dueDate,
                        isRecurring = item.isRecurring,
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
                handleComplete(note = null)
            }

            is ItemOccurrenceEvent.OnCompleteWithNoteConfirmClicked -> {
                _dialogState.update { ItemOccurrenceDialogState.None }
                handleComplete(event.note)
            }

            is ItemOccurrenceEvent.OnIgnoreClicked -> _dialogState.update { ItemOccurrenceDialogState.IgnoreConfirm }
            is ItemOccurrenceEvent.OnIgnoreConfirmClicked -> {
                _dialogState.update { ItemOccurrenceDialogState.None }
                handleIgnore(event.note)
            }

            is ItemOccurrenceEvent.OnExtendDeadlineClicked -> {
                val dueDate = uiState.value.dueDate ?: return
                _dialogState.update { ItemOccurrenceDialogState.ExtendDeadlineConfirm(dueDate) }
            }

            is ItemOccurrenceEvent.OnExtendDeadlineConfirmClicked -> {
                _dialogState.update { ItemOccurrenceDialogState.None }
                handleExtendDeadline(event.newDueDate)
            }

            is ItemOccurrenceEvent.OnDialogDismissed -> _dialogState.update { ItemOccurrenceDialogState.None }

            is ItemOccurrenceEvent.OnItemUpdatedExternally -> handleItemUpdatedExternally(event.item)
        }
    }

    private fun handleItemUpdatedExternally(item: Item) {
        val current = originalItem
        originalItem = current?.copy(
            title = item.title,
            description = item.description,
            sectionId = item.sectionId,
            dueDate = item.dueDate,
            dueTime = item.dueTime,
            recurrence = item.recurrence,
            reminderWarning = item.reminderWarning,
            tags = item.tags,
        ) ?: item
        _uiState.update {
            it.copy(
                isCompleted = item.isCompleted,
                completedAt = item.completedAt,
                dueDate = item.dueDate,
                isRecurring = item.isRecurring,
            )
        }
    }

    private fun handleCompleteClicked() {
        val item = originalItem ?: return
        when {
            uiState.value.isCompleted -> _dialogState.update { ItemOccurrenceDialogState.ReopenConfirm }
            item.isRecurring || uiState.value.isLate -> _dialogState.update {
                ItemOccurrenceDialogState.CompleteConfirm(isLate = uiState.value.isLate)
            }

            else -> handleComplete(note = null)
        }
    }

    private fun handleComplete(note: String?) = viewModelScope.launch {
        val item = originalItem ?: return@launch
        if (item.type != ItemType.TASK) return@launch
        val now = LocalDateTime.now()
        itemOccurrenceUseCase.complete(item, now, note)
            .onSuccess { result ->
                val updated = itemFormUseCase.get(item.id).first() ?: return@onSuccess
                originalItem = updated
                _uiState.update {
                    it.copy(
                        isCompleted = updated.isCompleted,
                        completedAt = updated.completedAt,
                        dueDate = updated.dueDate,
                        isRecurring = updated.isRecurring,
                    )
                }
                sendUiAction(ItemOccurrenceUiAction.ItemPersisted(updated))
                if (result == CompletionResult.Completed) {
                    sendUiAction(ItemOccurrenceUiAction.ShowSnackbar(R.string.item_detail_completed_snackbar))
                    sendUiAction(ItemOccurrenceUiAction.NavigateBack)
                }
            }
            .onFailure { sendUiAction(ItemOccurrenceUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleIgnore(note: String) = viewModelScope.launch {
        val item = originalItem ?: return@launch
        itemOccurrenceUseCase.ignore(item, note)
            .onSuccess { updated ->
                originalItem = updated
                _uiState.update { it.copy(dueDate = updated.dueDate, isRecurring = updated.isRecurring) }
                sendUiAction(ItemOccurrenceUiAction.ItemPersisted(updated))
            }
            .onFailure { sendUiAction(ItemOccurrenceUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleExtendDeadline(newDueDate: LocalDate) = viewModelScope.launch {
        val item = originalItem ?: return@launch
        itemOccurrenceUseCase.extendDueDate(item, newDueDate)
            .onSuccess { updated ->
                originalItem = updated
                _uiState.update { it.copy(dueDate = updated.dueDate, isRecurring = updated.isRecurring) }
                sendUiAction(ItemOccurrenceUiAction.ItemPersisted(updated))
            }
            .onFailure { sendUiAction(ItemOccurrenceUiAction.ShowError(it.message.orEmpty())) }
    }

    private suspend fun sendUiAction(action: ItemOccurrenceUiAction) = _uiAction.send(action)
}
