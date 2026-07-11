package com.seucaio.unideas.core.common.crud

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
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

/**
 * Generic MVI ViewModel for a "manage a list of named entities" screen (Sections, Tags, ...).
 * Entity-specific behavior comes in entirely through [operations] and the three string
 * resources — this class has no knowledge of the concrete entity type beyond [T].
 */
@OptIn(ExperimentalCoroutinesApi::class)
open class EntityCrudViewModel<T>(
    private val operations: EntityCrudOperations<T>,
    @StringRes private val loadErrorRes: Int,
    @StringRes private val nameRequiredRes: Int,
    @StringRes private val deleteBlockedRes: Int,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // region States

    val uiState: StateFlow<EntityCrudUiState<T>> = retryTrigger.flatMapLatest {
        operations.getAll()
            .map<List<T>, EntityCrudUiState<T>> { EntityCrudUiState.Success(it) }
            .onStart { emit(EntityCrudUiState.Loading) }
            .catch { emit(EntityCrudUiState.Error(loadErrorRes)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), EntityCrudUiState.Loading)

    // Which entity dialog is open — a separate StateFlow (not local Compose state, not nested
    // in uiState) so previews can simulate every dialog scenario via a PreviewProvider.
    private val _dialogState = MutableStateFlow<EntityDialogState<T>>(EntityDialogState.None)
    val dialogState: StateFlow<EntityDialogState<T>> = _dialogState.asStateFlow()

    // endregion

    private val _uiAction = Channel<EntityUiAction>(Channel.BUFFERED)
    val uiAction: Flow<EntityUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: EntityEvent<T>) {
        when (event) {
            is EntityEvent.OnAddClicked -> handleDialog(EntityDialogState.Add)
            is EntityEvent.OnAddConfirmClicked -> handleAdd(event.name)
            is EntityEvent.OnRenameClicked -> handleDialog(EntityDialogState.Rename(event.item))
            is EntityEvent.OnRenameConfirmClicked -> handleRename(event.newName)
            is EntityEvent.OnDeleteClicked -> handleDialog(EntityDialogState.Delete(event.item))
            is EntityEvent.OnDeleteConfirmClicked -> handleDelete()
            is EntityEvent.OnDialogDismissed -> handleDialog(EntityDialogState.None)
            is EntityEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleDialog(state: EntityDialogState<T>) = _dialogState.update { state }

    private fun handleAdd(name: String) = viewModelScope.launch {
        operations.add(name)
            .onSuccess { handleDialog(EntityDialogState.None) }
            .onFailure { handleFailure(it) }
    }

    private fun handleRename(newName: String) = viewModelScope.launch {
        val item = (_dialogState.value as? EntityDialogState.Rename)?.item ?: return@launch
        operations.rename(item, newName)
            .onSuccess { handleDialog(EntityDialogState.None) }
            .onFailure { handleFailure(it) }
    }

    private fun handleDelete() = viewModelScope.launch {
        val item = (_dialogState.value as? EntityDialogState.Delete)?.item ?: return@launch
        handleDialog(EntityDialogState.None)
        operations.delete(item)
            .onSuccess { handleDeletionStatus(it) }
            .onFailure { handleFailure(it) }
    }

    private suspend fun handleDeletionStatus(status: DeletionStatus) {
        if (status is DeletionStatus.BlockedByLinkedItems) {
            sendUiAction(EntityUiAction.ShowSnackbar(deleteBlockedRes, listOf(status.count)))
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        if (error is IllegalArgumentException) {
            EntityUiAction.ShowSnackbar(nameRequiredRes)
        } else {
            EntityUiAction.ShowError(error.message.orEmpty())
        }.let { sendUiAction(it) }
    }

    private suspend fun sendUiAction(action: EntityUiAction) = _uiAction.send(action)
}
