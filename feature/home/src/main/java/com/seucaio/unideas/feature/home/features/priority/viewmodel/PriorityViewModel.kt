package com.seucaio.unideas.feature.home.features.priority.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.usecase.item.HomeUseCase
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
import java.time.LocalDate

/** Exposes [itemsState]/[uiState] as independent `StateFlow`s — priority panel only. */
@OptIn(ExperimentalCoroutinesApi::class)
class PriorityViewModel(
    private val homeUseCase: HomeUseCase,
) : ViewModel() {

    //region itemsState

    // Query failures degrade to an empty list — no error state to retry from.
    val itemsState: StateFlow<PriorityItemsState> =
        homeUseCase.getPriorityItems(today = LocalDate.now(), dueSoonDays = Constants.DUE_SOON_DAYS)
            .catch { emit(emptyList()) }
            .map { priorityItems ->
                PriorityItemsState(
                    priorityItems = priorityItems.take(Constants.PRIORITY_PANEL_LIMIT),
                    showSeeAllButton = priorityItems.size > Constants.PRIORITY_PANEL_LIMIT,
                )
            }
            .stateIn(viewModelScope, WhileSubscribed(5_000), PriorityItemsState())

    //endregion

    //region uiState

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<PriorityUiState> = retryTrigger
        .flatMapLatest {
            homeUseCase.hasAnyItem()
                .map<Boolean, PriorityUiState> { PriorityUiState.Success(hasAnyItem = it) }
                .onStart { emit(PriorityUiState.Loading) }
                .catch { emit(PriorityUiState.Error(R.string.home_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), PriorityUiState.Loading)

    //endregion

    //region one-shot navigation/snackbar events

    private val _uiAction = Channel<PriorityUiAction>(Channel.BUFFERED)
    val uiAction: Flow<PriorityUiAction> = _uiAction.receiveAsFlow()

    //endregion

    fun onEvent(event: PriorityEvent) {
        when (event) {
            is PriorityEvent.OnItemClicked -> sendUiAction(PriorityUiAction.NavigateToDetail(event.itemId))
            is PriorityEvent.OnSeeAllClicked -> sendUiAction(PriorityUiAction.NavigateToAllPriorities)
            is PriorityEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun sendUiAction(action: PriorityUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
