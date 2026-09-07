package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.usecase.item.ItemLinkUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemLinksViewModel(
    private val itemId: Long?,
    private val itemLinkUseCase: ItemLinkUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<ItemLinksUiState> = (
        itemId?.let { id ->
            retryTrigger.flatMapLatest {
                itemLinkUseCase.getLinkedItems(id)
                    .map<List<Item>, ItemLinksUiState> { ItemLinksUiState.Success(it) }
                    .onStart { emit(ItemLinksUiState.Loading) }
                    .catch { emit(ItemLinksUiState.Error(R.string.item_links_load_error)) }
            }
        } ?: flowOf(ItemLinksUiState.Success())
        ).stateIn(viewModelScope, WhileSubscribed(5_000), ItemLinksUiState.Loading)

    private val _uiAction = Channel<ItemLinksUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemLinksUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: ItemLinksEvent) {
        when (event) {
            is ItemLinksEvent.OnLinkedItemClicked ->
                sendUiAction(ItemLinksUiAction.NavigateToDetail(event.itemId))

            is ItemLinksEvent.OnUnlinkClicked -> handleUnlink(event.itemId)

            is ItemLinksEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleUnlink(otherItemId: Long) {
        val id = itemId ?: return
        viewModelScope.launch {
            itemLinkUseCase.unlink(id, otherItemId)
                .onFailure { sendUiAction(ItemLinksUiAction.ShowError(it.message.orEmpty())) }
        }
    }

    private fun sendUiAction(action: ItemLinksUiAction) {
        viewModelScope.launch { _uiAction.send(action) }
    }
}
