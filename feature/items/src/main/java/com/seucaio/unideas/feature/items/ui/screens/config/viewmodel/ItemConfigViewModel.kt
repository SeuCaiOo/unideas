package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.Tag
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
import kotlinx.coroutines.runBlocking

class ItemConfigViewModel(
    private val itemId: Long,
    private val itemFormUseCase: ItemFormUseCase,
) : ViewModel() {

    private var originalItem: Item? = null
    private var availableTags: List<Tag> = emptyList()
    private var hasPendingSave = false

    private val _uiState = MutableStateFlow(ItemConfigUiState())
    val uiState: StateFlow<ItemConfigUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<ItemConfigDialogState>(ItemConfigDialogState.None)
    val dialogState: StateFlow<ItemConfigDialogState> = _dialogState.asStateFlow()

    private val _uiAction = Channel<ItemConfigUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemConfigUiAction> = _uiAction.receiveAsFlow()

    init {
        viewModelScope.launch { loadItem() }
    }

    private suspend fun loadItem() {
        val item = runCatching { itemFormUseCase.get(itemId).first() }.getOrNull()
        if (item == null) {
            sendUiAction(ItemConfigUiAction.ShowSnackbar(R.string.item_form_load_error))
            _uiState.update { it.markLoadFailed() }
            return
        }
        originalItem = item
        _uiState.update { it.applyLoadedItem(item) }
    }

    fun onEvent(event: ItemConfigEvent) {
        when (event) {
            is ItemConfigEvent.FieldEvent -> handleFieldEvent(event)
            is ItemConfigEvent.OnChangeTypeClicked ->
                _dialogState.update { ItemConfigDialogState.TypeSwitchConfirm(event.newType) }

            is ItemConfigEvent.OnDialogDismissed -> _dialogState.update { ItemConfigDialogState.None }
            is ItemConfigEvent.OnTypeSwitchConfirmClicked -> handleTypeSwitchConfirmed()
            is ItemConfigEvent.OnRetryClicked -> retryLoad()
            is ItemConfigEvent.OnAvailableTagsSynced -> availableTags = event.tags
        }
    }

    private fun handleFieldEvent(event: ItemConfigEvent.FieldEvent) {
        _uiState.update { state ->
            when (event) {
                is ItemConfigEvent.OnSectionChanged -> state.copy(sectionId = event.sectionId)
                is ItemConfigEvent.OnTagToggled -> state.toggleTag(event.tagId)
                is ItemConfigEvent.OnReminderToggled -> state.toggleReminder(event.enabled)
                is ItemConfigEvent.OnDueDateChanged -> state.changeDueDate(event.dueDate)
                is ItemConfigEvent.OnDueTimeChanged -> state.copy(dueTime = event.dueTime)
                is ItemConfigEvent.OnRecurrenceChanged -> state.changeRecurrence(event.recurrence)
                is ItemConfigEvent.OnReminderWarningChanged -> state.copy(reminderWarning = event.reminderWarning)
            }
        }
        hasPendingSave = true
        viewModelScope.launch {
            persist()
            hasPendingSave = false
        }
    }

    override fun onCleared() {
        if (hasPendingSave) {
            runBlocking { persist() }
        }
        super.onCleared()
    }

    private fun handleTypeSwitchConfirmed() = viewModelScope.launch {
        val dialog =
            _dialogState.value as? ItemConfigDialogState.TypeSwitchConfirm ?: return@launch
        val original = originalItem ?: return@launch
        val updated = uiState.value.switchedType(original, dialog.newType)

        itemFormUseCase.edit(updated)
            .onSuccess {
                originalItem = updated
                _uiState.update { it.applyLoadedItem(updated) }
            }
            .onFailure { sendUiAction(ItemConfigUiAction.ShowError(it.message.orEmpty())) }

        _dialogState.update { ItemConfigDialogState.None }
    }

    private suspend fun persist() {
        val original = originalItem ?: return
        val updated = uiState.value.toItem(original, availableTags)
        itemFormUseCase.edit(updated)
            .onSuccess { originalItem = updated }
            .onFailure { sendUiAction(ItemConfigUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun retryLoad() {
        _uiState.update { it.startLoading() }
        viewModelScope.launch { loadItem() }
    }

    private suspend fun sendUiAction(action: ItemConfigUiAction) = _uiAction.send(action)
}
