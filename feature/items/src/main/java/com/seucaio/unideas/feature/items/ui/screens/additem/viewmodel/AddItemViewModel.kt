package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueDate
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueTime
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableRecurrence
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableReminderWarning
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddItemViewModel(
    private val createItem: CreateItemUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
    initialType: ItemType = ItemType.TASK,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState(type = initialType))
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<AddItemUiAction>(Channel.BUFFERED)
    val uiAction: Flow<AddItemUiAction> = _uiAction.receiveAsFlow()

    init {
        viewModelScope.launch { loadFormData() }
    }

    private suspend fun loadFormData() {
        runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
            _uiState.update { it.setReferenceData(referenceData.sections, referenceData.tags) }
        }
    }

    fun onEvent(event: AddItemEvent) {
        when (event) {
            is AddItemEvent.FieldEvent -> _uiState.update { it.reduce(event) }
            is AddItemEvent.OnSaveClicked -> handleSave()
        }
    }

    private fun handleSave() = viewModelScope.launch {
        val state = _uiState.value
        val selectedTags = state.availableTags.filter { it.id in state.selectedTagIds }

        createItem(
            Item(
                type = state.type,
                title = state.title,
                description = state.description.ifBlank { null },
                sectionId = state.sectionId,
                dueDate = state.persistableDueDate,
                dueTime = state.persistableDueTime,
                recurrence = state.persistableRecurrence,
                reminderWarning = state.persistableReminderWarning,
                createdAt = LocalDateTime.now(),
                tags = selectedTags,
            ),
        ).onSuccess { sendUiAction(AddItemUiAction.NavigateBack) }.onFailure { handleFailure(it) }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            sendUiAction(AddItemUiAction.ShowSnackbar(R.string.item_title_required))
        } else {
            sendUiAction(AddItemUiAction.ShowError(error.message.orEmpty()))
        }
    }

    private suspend fun sendUiAction(action: AddItemUiAction) = _uiAction.send(action)
}
