package com.seucaio.unideas.feature.tags.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import com.seucaio.unideas.feature.tags.R
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
class TagsViewModel(
    private val getTags: GetTagsUseCase,
    private val addTag: AddTagUseCase,
    private val renameTag: RenameTagUseCase,
    private val deleteTag: DeleteTagUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // region States

    val uiState: StateFlow<TagsUiState> = retryTrigger.flatMapLatest {
        getTags()
            .map<List<Tag>, TagsUiState> { TagsUiState.Success(it) }
            .onStart { emit(TagsUiState.Loading) }
            .catch { emit(TagsUiState.Error(R.string.tags_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), TagsUiState.Loading)

    // Which entity dialog is open — a separate StateFlow (not local Compose state, not nested
    // in uiState) so previews can simulate every dialog scenario via TagsPreviewProvider.
    private val _dialogState = MutableStateFlow<TagsDialogState>(TagsDialogState.None)
    val dialogState: StateFlow<TagsDialogState> = _dialogState.asStateFlow()

    // endregion

    private val _uiAction = Channel<TagsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<TagsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: TagsEvent) {
        when (event) {
            is TagsEvent.OnAddClicked -> handleDialog(TagsDialogState.Add)
            is TagsEvent.OnAddConfirmClicked -> handleAdd(event.name)
            is TagsEvent.OnRenameClicked -> handleDialog(TagsDialogState.Rename(event.tag))
            is TagsEvent.OnRenameConfirmClicked -> handleRename(event.newName)
            is TagsEvent.OnDeleteClicked -> handleDialog(TagsDialogState.Delete(event.tag))
            is TagsEvent.OnDeleteConfirmClicked -> handleDelete()
            is TagsEvent.OnDialogDismissed -> handleDialog(TagsDialogState.None)
            is TagsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleDialog(state: TagsDialogState) = _dialogState.update { state }

    private fun handleAdd(name: String) = viewModelScope.launch {
        addTag(name)
            .onSuccess { handleDialog(TagsDialogState.None) }
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(newName: String) = viewModelScope.launch {
        val tag = (_dialogState.value as? TagsDialogState.Rename)?.tag ?: return@launch
        renameTag(tag.copy(name = newName))
            .onSuccess { handleDialog(TagsDialogState.None) }
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete() = viewModelScope.launch {
        val tag = (_dialogState.value as? TagsDialogState.Delete)?.tag ?: return@launch
        handleDialog(TagsDialogState.None)
        deleteTag(tag.id)
            .onSuccess { handleDeletionStatus(it) }
            .onFailure { handleFailure(it) }
    }

    private suspend fun handleDeletionStatus(status: DeletionStatus) {
        if (status is DeletionStatus.BlockedByLinkedItems) {
            sendUiAction(TagsUiAction.ShowSnackbar(R.string.tag_delete_blocked, listOf(status.count)))
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            TagsUiAction.ShowSnackbar(R.string.tag_name_required)
        } else {
            TagsUiAction.ShowError(error.message.orEmpty())
        }.let { sendUiAction(it) }
    }

    private suspend fun sendUiAction(action: TagsUiAction) = _uiAction.send(action)
}
