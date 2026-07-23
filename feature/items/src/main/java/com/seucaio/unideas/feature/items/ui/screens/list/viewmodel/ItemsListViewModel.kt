package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
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

/**
 * Dev-only listing of every [Item], regardless of type — no tabs/filters/priority panel,
 * that's Home's (#27) scope. Discardable once Home ships (#62).
 *
 * There is no type-agnostic query in `:domain`/`:data` — [GetItemsUseCase] is scoped per Home
 * tab. Rather than add a throwaway "get all items" method there, this combines the TASK and
 * NOTE flows here and re-sorts, keeping the extra glue inside this disposable screen.
 */
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
            is ItemsListEvent.OnAddClicked -> sendUiAction(ItemsListUiAction.NavigateToForm)
            is ItemsListEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun sendUiAction(action: ItemsListUiAction) = viewModelScope.launch { _uiAction.send(action) }
}
