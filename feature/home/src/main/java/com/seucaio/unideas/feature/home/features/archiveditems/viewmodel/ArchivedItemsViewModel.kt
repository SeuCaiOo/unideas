package com.seucaio.unideas.feature.home.features.archiveditems.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.usecase.item.ItemArchiveUseCase
import com.seucaio.unideas.feature.home.R
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
class ArchivedItemsViewModel(private val itemArchiveUseCase: ItemArchiveUseCase) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<ArchivedItemsUiState> = retryTrigger.flatMapLatest {
        itemArchiveUseCase.getArchivedItems()
            .map<List<Item>, ArchivedItemsUiState> { ArchivedItemsUiState.Success(it) }
            .onStart { emit(ArchivedItemsUiState.Loading) }
            .catch { emit(ArchivedItemsUiState.Error(R.string.archived_items_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), ArchivedItemsUiState.Loading)

    private val _uiAction = Channel<ArchivedItemsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ArchivedItemsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: ArchivedItemsEvent) {
        when (event) {
            is ArchivedItemsEvent.OnItemClicked ->
                sendUiAction(ArchivedItemsUiAction.NavigateToDetail(event.itemId))

            is ArchivedItemsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun sendUiAction(action: ArchivedItemsUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
