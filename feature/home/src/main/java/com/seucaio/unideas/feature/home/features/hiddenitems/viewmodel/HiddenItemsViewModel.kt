package com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.usecase.item.GetConfidentialItemsUseCase
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
class HiddenItemsViewModel(private val getConfidentialItemsUseCase: GetConfidentialItemsUseCase) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<HiddenItemsUiState> = retryTrigger.flatMapLatest {
        getConfidentialItemsUseCase()
            .map<List<Item>, HiddenItemsUiState> { HiddenItemsUiState.Success(it) }
            .onStart { emit(HiddenItemsUiState.Loading) }
            .catch { emit(HiddenItemsUiState.Error(R.string.hidden_items_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), HiddenItemsUiState.Loading)

    private val _uiAction = Channel<HiddenItemsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<HiddenItemsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: HiddenItemsEvent) {
        when (event) {
            is HiddenItemsEvent.OnItemClicked ->
                sendUiAction(HiddenItemsUiAction.NavigateToDetail(event.itemId))

            is HiddenItemsEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun sendUiAction(action: HiddenItemsUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
