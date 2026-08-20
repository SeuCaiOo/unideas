package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class ItemHistoryViewModel(
    private val itemId: Long,
    private val itemOccurrenceUseCase: ItemOccurrenceUseCase,
) : ViewModel() {

    private val activeFilter = MutableStateFlow(HistoryFilter.ALL)

    val uiState: StateFlow<ItemHistoryUiState> =
        combine(itemOccurrenceUseCase.getHistory(itemId), activeFilter) { history, filter ->
            ItemHistoryUiState(history = history, activeFilter = filter)
        }.stateIn(viewModelScope, WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS), ItemHistoryUiState())

    private val _dialogState = MutableStateFlow<ItemHistoryDialogState>(ItemHistoryDialogState.None)
    val dialogState: StateFlow<ItemHistoryDialogState> = _dialogState.asStateFlow()

    private val _uiAction = Channel<ItemHistoryUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemHistoryUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: ItemHistoryEvent) {
        when (event) {
            is ItemHistoryEvent.OnFilterSelected -> activeFilter.update { event.filter }
            is ItemHistoryEvent.OnAddEntryClicked ->
                handleDialog(
                    ItemHistoryDialogState.AddEditEntry(
                        blockedDates = blockedDates(
                            existing = null
                        )
                    )
                )

            is ItemHistoryEvent.OnEditEntryClicked ->
                handleDialog(
                    ItemHistoryDialogState.AddEditEntry(
                        event.entry,
                        blockedDates(existing = event.entry)
                    ),
                )

            is ItemHistoryEvent.OnDeleteEntryClicked ->
                handleDialog(ItemHistoryDialogState.DeleteConfirm(event.entry))
            is ItemHistoryEvent.OnDeleteConfirmClicked -> handleDeleteConfirmed()
            is ItemHistoryEvent.OnDialogDismissed -> handleDialog(ItemHistoryDialogState.None)
            is ItemHistoryEvent.OnEntrySubmitted ->
                handleEntrySubmitted(event.scheduledDate, event.completedAt, event.note)
        }
    }

    private fun handleDialog(state: ItemHistoryDialogState) = _dialogState.update { state }

    private fun blockedDates(existing: ItemCompletionHistory?): Set<LocalDate> {
        val dates = uiState.value.history.map { it.scheduledDate }.toSet()
        return if (existing != null) dates - existing.scheduledDate else dates
    }

    private fun handleEntrySubmitted(
        scheduledDate: LocalDate,
        completedAt: LocalDateTime?,
        note: String?,
    ) = viewModelScope.launch {
        val existingId = (_dialogState.value as? ItemHistoryDialogState.AddEditEntry)?.existing?.id ?: 0L
        val record = ItemCompletionHistory(
            id = existingId,
            itemId = itemId,
            scheduledDate = scheduledDate,
            completedAt = completedAt,
            note = note,
        )
        itemOccurrenceUseCase.saveHistoryEntry(record)
            .onSuccess { handleDialog(ItemHistoryDialogState.None) }
            .onFailure {
                // Close the sheet before showing feedback — its ModalBottomSheet opens in a
                // separate platform window, so a snackbar anchored to this screen's Scaffold
                // would stay hidden behind it while the sheet is still open.
                handleDialog(ItemHistoryDialogState.None)
                handleFailure(it)
            }
    }

    private fun handleDeleteConfirmed() = viewModelScope.launch {
        val entry = (_dialogState.value as? ItemHistoryDialogState.DeleteConfirm)?.entry ?: return@launch
        itemOccurrenceUseCase.deleteHistoryEntry(entry.id)
            .onSuccess { handleDialog(ItemHistoryDialogState.None) }
            .onFailure {
                handleDialog(ItemHistoryDialogState.None)
                handleFailure(it)
            }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            sendUiAction(ItemHistoryUiAction.ShowSnackbar(R.string.item_history_entry_invalid))
        } else {
            sendUiAction(ItemHistoryUiAction.ShowError(error.message.orEmpty()))
        }
    }

    private suspend fun sendUiAction(action: ItemHistoryUiAction) = _uiAction.send(action)

    private companion object {
        const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L
    }
}
