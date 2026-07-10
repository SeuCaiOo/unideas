package com.seucaio.unideas.feature.sections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import com.seucaio.unideas.feature.sections.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SectionsViewModel(
    private val getSections: GetSectionsUseCase,
    private val addSection: AddSectionUseCase,
    private val renameSection: RenameSectionUseCase,
    private val deleteSection: DeleteSectionUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<SectionsUiState> = retryTrigger
        .flatMapLatest {
            getSections()
                .map<List<Section>, SectionsUiState> { SectionsUiState.Success(it) }
                .onStart { emit(SectionsUiState.Loading) }
                .catch { emit(SectionsUiState.Error(R.string.sections_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), SectionsUiState.Loading)

    private val _action = Channel<SectionsUiAction>(Channel.BUFFERED)
    val action: Flow<SectionsUiAction> = _action.receiveAsFlow()

    fun onEvent(event: SectionsEvent) {
        when (event) {
            is SectionsEvent.OnAddClicked -> handleAdd(event.name)
            is SectionsEvent.OnRenameClicked -> handleRename(event.section, event.newName)
            is SectionsEvent.OnDeleteClicked -> handleDelete(event.id)
            is SectionsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleAdd(name: String) = viewModelScope.launch {
        addSection(name)
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(section: Section, newName: String) = viewModelScope.launch {
        renameSection(section.copy(name = newName))
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete(id: Long) = viewModelScope.launch {
        deleteSection(id)
            .onSuccess { handleDeletionStatus(it) }
            .onFailure { handleFailure(it) }
    }

    private suspend fun handleDeletionStatus(status: DeletionStatus) {
        if (status is DeletionStatus.BlockedByLinkedItems) {
            _action.send(SectionsUiAction.ShowSnackbar(R.string.section_delete_blocked, listOf(status.count)))
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            _action.send(SectionsUiAction.ShowSnackbar(R.string.section_name_required))
        } else {
            _action.send(SectionsUiAction.ShowError(error.message.orEmpty()))
        }
    }
}
