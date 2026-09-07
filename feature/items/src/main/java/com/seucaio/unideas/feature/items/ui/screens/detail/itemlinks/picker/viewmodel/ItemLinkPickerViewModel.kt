package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.usecase.item.GetItemsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemLinkUseCase
import com.seucaio.unideas.feature.items.R
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

@OptIn(ExperimentalCoroutinesApi::class)
class ItemLinkPickerViewModel(
    private val itemId: Long,
    private val type: ItemType,
    private val getItemsUseCase: GetItemsUseCase,
    private val itemLinkUseCase: ItemLinkUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<ItemLinkPickerUiState> = retryTrigger.flatMapLatest {
        combine(
            getItemsUseCase(type),
            itemLinkUseCase.getLinkedItems(itemId),
            selectedIds,
        ) { items, linked, selected ->
            val linkedIds = linked.map(Item::id).toSet()
            ItemLinkPickerUiState.Success(
                items = items.filter { it.id != itemId && it.id !in linkedIds },
                selectedIds = selected,
            )
        }
            .map<ItemLinkPickerUiState.Success, ItemLinkPickerUiState> { it }
            .onStart { emit(ItemLinkPickerUiState.Loading) }
            .catch { emit(ItemLinkPickerUiState.Error(R.string.item_links_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), ItemLinkPickerUiState.Loading)

    private val _uiAction = Channel<ItemLinkPickerUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemLinkPickerUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: ItemLinkPickerEvent) {
        when (event) {
            is ItemLinkPickerEvent.OnItemToggled -> toggleItemSelection(event.itemId)

            is ItemLinkPickerEvent.OnConfirmClicked -> handleConfirm()
            is ItemLinkPickerEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun toggleItemSelection(itemId: Long) {
        selectedIds.update { if (itemId in it) it - itemId else it + itemId }
    }

    private fun handleConfirm() {
        val ids = selectedIds.value
        if (ids.isEmpty()) {
            sendUiAction(ItemLinkPickerUiAction.NavigateBack)
            return
        }
        viewModelScope.launch {
            ids.forEach { otherId ->
                itemLinkUseCase.link(itemId, otherId).onFailure {
                    sendUiAction(ItemLinkPickerUiAction.ShowError(it.message.orEmpty()))
                }
            }
            sendUiAction(ItemLinkPickerUiAction.NavigateBack)
        }
    }

    private fun sendUiAction(action: ItemLinkPickerUiAction) {
        viewModelScope.launch { _uiAction.send(action) }
    }
}
