package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ItemHistoryViewModel(
    private val itemId: Long,
    itemOccurrenceUseCase: ItemOccurrenceUseCase,
) : ViewModel() {

    private val activeFilter = MutableStateFlow(HistoryFilter.ALL)

    val uiState: StateFlow<ItemHistoryUiState> =
        combine(itemOccurrenceUseCase.getHistory(itemId), activeFilter) { history, filter ->
            ItemHistoryUiState(history = history, activeFilter = filter)
        }.stateIn(viewModelScope, WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS), ItemHistoryUiState())

    fun onEvent(event: ItemHistoryEvent) {
        when (event) {
            is ItemHistoryEvent.OnFilterSelected -> activeFilter.update { event.filter }
        }
    }

    private companion object {
        const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L
    }
}
