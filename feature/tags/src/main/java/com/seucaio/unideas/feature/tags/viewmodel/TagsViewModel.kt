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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagsViewModel(
    getTags: GetTagsUseCase,
    private val addTag: AddTagUseCase,
    private val renameTag: RenameTagUseCase,
    private val deleteTag: DeleteTagUseCase,
) : ViewModel() {

    val uiState: StateFlow<TagsUiState> = getTags()
        .map<List<Tag>, TagsUiState> { TagsUiState.Success(it) }
        .catch { emit(TagsUiState.Error(R.string.tags_load_error)) }
        .stateIn(viewModelScope, WhileSubscribed(5_000), TagsUiState.Loading)

    private val _action = Channel<TagsUiAction>(Channel.BUFFERED)
    val action: Flow<TagsUiAction> = _action.receiveAsFlow()

    fun onEvent(event: TagsEvent) {
        when (event) {
            is TagsEvent.OnAddClicked -> handleAdd(event.name)
            is TagsEvent.OnRenameClicked -> handleRename(event.tag, event.newName)
            is TagsEvent.OnDeleteClicked -> handleDelete(event.id)
        }
    }

    private fun handleAdd(name: String) = viewModelScope.launch {
        addTag(name)
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(tag: Tag, newName: String) = viewModelScope.launch {
        renameTag(tag.copy(name = newName))
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete(id: Long) = viewModelScope.launch {
        deleteTag(id)
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
