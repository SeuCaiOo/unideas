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
private sealed interface TagsResult {
    data object Loading : TagsResult
    data object Failure : TagsResult
    data class Data(val tags: List<Tag>) : TagsResult
}

@OptIn(ExperimentalCoroutinesApi::class)
class TagsViewModel(
    private val getTags: GetTagsUseCase,
    private val addTag: AddTagUseCase,
    private val renameTag: RenameTagUseCase,
    private val deleteTag: DeleteTagUseCase,
) : ViewModel() {

    // Which entity dialog is open — lives here (not as local Compose state) so previews can
    // simulate every dialog scenario via TagsPreviewProvider, not just the three UiState variants.
    private val dialogState = MutableStateFlow<TagsDialogState>(TagsDialogState.None)

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val tagsResult: Flow<TagsResult> = retryTrigger.flatMapLatest {
        getTags()
            .map<List<Tag>, TagsResult> { TagsResult.Data(it) }
            .onStart { emit(TagsResult.Loading) }
            .catch { emit(TagsResult.Failure) }
    }

    val uiState: StateFlow<TagsUiState> = combine(dialogState, tagsResult) { dialog, result ->
        when (result) {
            is TagsResult.Loading -> TagsUiState.Loading
            is TagsResult.Failure -> TagsUiState.Error(R.string.tags_load_error)
            is TagsResult.Data -> TagsUiState.Success(tags = result.tags, dialog = dialog)
        }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), TagsUiState.Loading)

    private val _action = Channel<TagsUiAction>(Channel.BUFFERED)
    val action: Flow<TagsUiAction> = _action.receiveAsFlow()

    fun onEvent(event: TagsEvent) {
        when (event) {
            is TagsEvent.OnAddClicked -> dialogState.update { TagsDialogState.Add }
            is TagsEvent.OnAddConfirmClicked -> handleAdd(event.name)
            is TagsEvent.OnRenameClicked -> dialogState.update { TagsDialogState.Rename(event.tag) }
            is TagsEvent.OnRenameConfirmClicked -> handleRename(event.newName)
            is TagsEvent.OnDeleteClicked -> dialogState.update { TagsDialogState.Delete(event.tag) }
            is TagsEvent.OnDeleteConfirmClicked -> handleDelete()
            is TagsEvent.OnDialogDismissed -> dialogState.update { TagsDialogState.None }
            is TagsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleAdd(name: String) = viewModelScope.launch {
        addTag(name)
            .onSuccess { dialogState.update { TagsDialogState.None } }
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(newName: String) = viewModelScope.launch {
        val tag = (dialogState.value as? TagsDialogState.Rename)?.tag ?: return@launch
        renameTag(tag.copy(name = newName))
            .onSuccess { dialogState.update { TagsDialogState.None } }
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete() = viewModelScope.launch {
        val tag = (dialogState.value as? TagsDialogState.Delete)?.tag ?: return@launch
        dialogState.update { TagsDialogState.None }
        deleteTag(tag.id)
            .onSuccess { handleDeletionStatus(it) }
            .onFailure { handleFailure(it) }
    }

    private suspend fun handleDeletionStatus(status: DeletionStatus) {
        if (status is DeletionStatus.BlockedByLinkedItems) {
            _action.send(TagsUiAction.ShowSnackbar(R.string.tag_delete_blocked, listOf(status.count)))
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            _action.send(TagsUiAction.ShowSnackbar(R.string.tag_name_required))
        } else {
            _action.send(TagsUiAction.ShowError(error.message.orEmpty()))
        }
    }
}
