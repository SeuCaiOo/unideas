package com.seucaio.unideas.feature.sections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.feature.sections.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SectionsViewModel(
    private val sectionUseCase: SectionUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // region States

    val uiState: StateFlow<SectionsUiState> = retryTrigger.flatMapLatest {
        sectionUseCase.getAll()
            .map<List<Section>, SectionsUiState> { SectionsUiState.Success(it) }
            .onStart { emit(SectionsUiState.Loading) }
            .catch { emit(SectionsUiState.Error(R.string.sections_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), SectionsUiState.Loading)

    // Which entity dialog is open — a separate StateFlow (not local Compose state, not nested
    // in uiState) so previews can simulate every dialog scenario via SectionsPreviewProvider.
    private val _dialogState = MutableStateFlow<SectionsDialogState>(SectionsDialogState.None)
    val dialogState: StateFlow<SectionsDialogState> = _dialogState.asStateFlow()

    // endregion

    private val _uiAction = Channel<SectionsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<SectionsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: SectionsEvent) {
        when (event) {
            is SectionsEvent.OnAddClicked -> handleDialog(SectionsDialogState.Add)
            is SectionsEvent.OnAddConfirmClicked -> handleAdd(event.name)
            is SectionsEvent.OnRenameClicked -> handleDialog(SectionsDialogState.Rename(event.section))
            is SectionsEvent.OnRenameConfirmClicked -> handleRename(event.newName)
            is SectionsEvent.OnDeleteClicked -> handleDialog(SectionsDialogState.Delete(event.section))
            is SectionsEvent.OnDeleteConfirmClicked -> handleDelete()
            is SectionsEvent.OnDialogDismissed -> handleDialog(SectionsDialogState.None)
            is SectionsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleDialog(state: SectionsDialogState) = _dialogState.update { state }

    private fun handleAdd(name: String) = viewModelScope.launch {
        sectionUseCase.add(name)
            .onSuccess { handleDialog(SectionsDialogState.None) }
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(newName: String) = viewModelScope.launch {
        val section = (_dialogState.value as? SectionsDialogState.Rename)?.section ?: return@launch
        sectionUseCase.rename(section.copy(name = newName))
            .onSuccess { handleDialog(SectionsDialogState.None) }
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete() = viewModelScope.launch {
        val section = (_dialogState.value as? SectionsDialogState.Delete)?.section ?: return@launch
        handleDialog(SectionsDialogState.None)
        sectionUseCase.delete(section.id)
            .onSuccess { handleDeletionStatus(it) }
            .onFailure { handleFailure(it) }
    }

    private suspend fun handleDeletionStatus(status: DeletionStatus) {
        if (status is DeletionStatus.BlockedByLinkedItems) {
            sendUiAction(SectionsUiAction.ShowSnackbar(R.string.section_delete_blocked, listOf(status.count)))
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            SectionsUiAction.ShowSnackbar(R.string.section_name_required)
        } else {
            SectionsUiAction.ShowError(error.message.orEmpty())
        }.let { sendUiAction(it) }
    }

    private suspend fun sendUiAction(action: SectionsUiAction) = _uiAction.send(action)
}
