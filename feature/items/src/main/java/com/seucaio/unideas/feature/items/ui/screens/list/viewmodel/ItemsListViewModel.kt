package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.usecase.item.GetItemsUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsListViewModel(private val getItems: GetItemsUseCase) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<ItemsListUiState> = retryTrigger.flatMapLatest {
        combine(getItems(ItemType.TASK), getItems(ItemType.NOTE)) { tasks, notes ->
            ItemsListUiState.Success((tasks + notes).sortedByDescending { it.createdAt }) as ItemsListUiState
        }
            .onStart { emit(ItemsListUiState.Loading) }
            .catch { emit(ItemsListUiState.Error(R.string.items_list_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), ItemsListUiState.Loading)

    private val _uiAction = Channel<ItemsListUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemsListUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: ItemsListEvent) {
        when (event) {
            is ItemsListEvent.OnItemClicked -> sendUiAction(ItemsListUiAction.NavigateToDetail(event.itemId))
            is ItemsListEvent.OnAddClicked -> sendUiAction(ItemsListUiAction.NavigateToAddItem)
            is ItemsListEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun sendUiAction(action: ItemsListUiAction) = viewModelScope.launch { _uiAction.send(action) }
}
