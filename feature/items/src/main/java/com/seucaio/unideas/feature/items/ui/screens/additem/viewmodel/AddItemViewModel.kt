package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.feature.items.R
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
            _uiState.update {
                it.copy(availableSections = referenceData.sections, availableTags = referenceData.tags)
            }
        }
    }

    fun onEvent(event: AddItemEvent) {
        when (event) {
            is AddItemEvent.OnTypeChanged -> _uiState.update { it.changeType(event.type) }
            is AddItemEvent.OnTitleChanged -> _uiState.update { it.changeTitle(event.title) }
            is AddItemEvent.OnDescriptionChanged -> _uiState.update { it.changeDescription(event.description) }
            is AddItemEvent.OnSectionChanged -> _uiState.update { it.setSection(event.sectionId) }
            is AddItemEvent.OnTagToggled -> _uiState.update { it.setTag(event.tagId) }
            is AddItemEvent.OnDueDateChanged -> _uiState.update {
                it.copy(
                    dueDate = event.dueDate,
                    recurrence = if (event.dueDate == null) Recurrence.None else it.recurrence,
                )
            }
            is AddItemEvent.OnRecurrenceChanged -> _uiState.update { it.copy(recurrence = event.recurrence) }
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
                dueDate = state.dueDate,
                recurrence = state.recurrence,
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
