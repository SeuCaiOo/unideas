package com.seucaio.unideas.feature.home.features.panel.viewmodel

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
class HomeViewModel(
    private val homeUseCase: HomeUseCase,
) : ViewModel() {

    //region itemsState

    // Query failures degrade to an empty list — no error state to retry from.
    val itemsState: StateFlow<HomeItemsState> =
        homeUseCase.getPriorityItems(today = LocalDate.now(), dueSoonDays = Constants.DUE_SOON_DAYS)
            .catch { emit(emptyList()) }
            .map { priorityItems ->
                HomeItemsState(
                    priorityItems = priorityItems.take(Constants.PRIORITY_PANEL_LIMIT),
                    showSeeAllButton = priorityItems.size > Constants.PRIORITY_PANEL_LIMIT,
                )
            }
            .stateIn(viewModelScope, WhileSubscribed(5_000), HomeItemsState())

    //endregion

    //region uiState

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            homeUseCase.hasAnyItem()
                .map<Boolean, HomeUiState> { HomeUiState.Success(hasAnyItem = it) }
                .onStart { emit(HomeUiState.Loading) }
                .catch { emit(HomeUiState.Error(R.string.home_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), HomeUiState.Loading)

    //endregion

    //region one-shot navigation/snackbar events

    private val _uiAction = Channel<HomeUiAction>(Channel.BUFFERED)
    val uiAction: Flow<HomeUiAction> = _uiAction.receiveAsFlow()

    //endregion

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnItemClicked -> sendUiAction(HomeUiAction.NavigateToDetail(event.itemId))
            is HomeEvent.OnAddClicked -> sendUiAction(HomeUiAction.NavigateToAddItem(event.type))
            is HomeEvent.OnSeeAllClicked -> sendUiAction(HomeUiAction.NavigateToAllPriorities)
            is HomeEvent.OnSettingsClicked -> sendUiAction(HomeUiAction.NavigateToSettings)
            is HomeEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun sendUiAction(action: HomeUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
