package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

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

/**
 * ViewModel for the add-item screen — its only job is creating a new [Item] via
 * [CreateItemUseCase], called directly (no facade: a single-method need doesn't earn one, unlike
 * [com.seucaio.unideas.domain.usecase.item.ItemFormUseCase] which covers edit/delete/share/complete
 * for [com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel]). Same
 * no-`Loading`/`Error` shape as that ViewModel's `UiState` — fields always render, blank until
 * `loadFormData` fills in sections/tags.
 */
class ItemDetailViewModel(
    private val createItem: CreateItemUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
    initialType: ItemType = ItemType.TASK,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState(type = initialType))
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ItemDetailUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemDetailUiAction> = _uiAction.receiveAsFlow()

    init {
        viewModelScope.launch { loadFormData() }
    }

    private suspend fun loadFormData() {
        // Failure here (rare — GetSectionsAndTagsUseCase already falls back to empty lists on
        // its own) just leaves availableSections/availableTags empty, same as ItemFormViewModel.
        runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
            _uiState.update {
                it.copy(availableSections = referenceData.sections, availableTags = referenceData.tags)
            }
        }
    }

    fun onEvent(event: ItemDetailEvent) {
        when (event) {
            is ItemDetailEvent.OnTypeChanged -> _uiState.update { it.changeType(event.type) }
            is ItemDetailEvent.OnTitleChanged -> _uiState.update { it.changeTitle(event.title) }
            is ItemDetailEvent.OnDescriptionChanged -> _uiState.update { it.changeDescription(event.description) }
            is ItemDetailEvent.OnSectionChanged -> _uiState.update { it.setSection(event.sectionId) }
            is ItemDetailEvent.OnTagToggled -> _uiState.update { it.setTag(event.tagId) }
            is ItemDetailEvent.OnDueDateChanged -> _uiState.update {
                it.copy(
                    dueDate = event.dueDate,
                    recurrence = if (event.dueDate == null) Recurrence.None else it.recurrence,
                )
            }
            is ItemDetailEvent.OnRecurrenceChanged -> _uiState.update { it.copy(recurrence = event.recurrence) }
            is ItemDetailEvent.OnSaveClicked -> handleSave()
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
        ).onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }.onFailure { handleFailure(it) }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            sendUiAction(ItemDetailUiAction.ShowSnackbar(R.string.item_title_required))
        } else {
            sendUiAction(ItemDetailUiAction.ShowError(error.message.orEmpty()))
        }
    }

    private suspend fun sendUiAction(action: ItemDetailUiAction) = _uiAction.send(action)
}
