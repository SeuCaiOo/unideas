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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Result of the domain fetch, combined with [dialogState] (UI-only) to build [uiState]. */
private sealed interface SectionsResult {
    data object Loading : SectionsResult
    data object Failure : SectionsResult
    data class Data(val sections: List<Section>) : SectionsResult
}

@OptIn(ExperimentalCoroutinesApi::class)
class SectionsViewModel(
    private val getSections: GetSectionsUseCase,
    private val addSection: AddSectionUseCase,
    private val renameSection: RenameSectionUseCase,
    private val deleteSection: DeleteSectionUseCase,
) : ViewModel() {

    // Which entity dialog is open — lives here (not as local Compose state) so previews can
    // simulate every dialog scenario via SectionsPreviewProvider, not just the list states.
    private val dialogState = MutableStateFlow<SectionsDialogState>(SectionsDialogState.None)

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val sectionsResult: Flow<SectionsResult> = retryTrigger.flatMapLatest {
        getSections()
            .map<List<Section>, SectionsResult> { SectionsResult.Data(it) }
            .onStart { emit(SectionsResult.Loading) }
            .catch { emit(SectionsResult.Failure) }
    }

    val uiState: StateFlow<SectionsUiState> = combine(dialogState, sectionsResult) { dialog, result ->
        when (result) {
            is SectionsResult.Loading -> SectionsUiState.Loading
            is SectionsResult.Failure -> SectionsUiState.Error(R.string.sections_load_error)
            is SectionsResult.Data -> SectionsUiState.Success(sections = result.sections, dialog = dialog)
        }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), SectionsUiState.Loading)

    private val _action = Channel<SectionsUiAction>(Channel.BUFFERED)
    val action: Flow<SectionsUiAction> = _action.receiveAsFlow()

    fun onEvent(event: SectionsEvent) {
        when (event) {
            is SectionsEvent.OnAddClicked -> dialogState.update { SectionsDialogState.Add }
            is SectionsEvent.OnAddConfirmClicked -> handleAdd(event.name)
            is SectionsEvent.OnRenameClicked -> dialogState.update { SectionsDialogState.Rename(event.section) }
            is SectionsEvent.OnRenameConfirmClicked -> handleRename(event.newName)
            is SectionsEvent.OnDeleteClicked -> dialogState.update { SectionsDialogState.Delete(event.section) }
            is SectionsEvent.OnDeleteConfirmClicked -> handleDelete()
            is SectionsEvent.OnDialogDismissed -> dialogState.update { SectionsDialogState.None }
            is SectionsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleAdd(name: String) = viewModelScope.launch {
        addSection(name)
            .onSuccess { dialogState.update { SectionsDialogState.None } }
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(newName: String) = viewModelScope.launch {
        val section = (dialogState.value as? SectionsDialogState.Rename)?.section ?: return@launch
        renameSection(section.copy(name = newName))
            .onSuccess { dialogState.update { SectionsDialogState.None } }
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete() = viewModelScope.launch {
        val section = (dialogState.value as? SectionsDialogState.Delete)?.section ?: return@launch
        dialogState.update { SectionsDialogState.None }
        deleteSection(section.id)
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
