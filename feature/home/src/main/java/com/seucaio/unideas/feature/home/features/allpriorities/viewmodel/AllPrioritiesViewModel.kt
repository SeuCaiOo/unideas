package com.seucaio.unideas.feature.home.features.allpriorities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel for the all-priorities screen (Home's "See all") — same [HomeUseCase.getPriorityItems]
 * flow as the fixed panel, without the panel's display limit.
 *
 * No UI-only state (no filters, no tabs) — follows Exception 1 of `mvi.md` ([SectionsViewModel]),
 * deriving `uiState` directly from the domain flow instead of `combine`/`InternalState`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllPrioritiesViewModel(
    private val homeUseCase: HomeUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<AllPrioritiesUiState> = retryTrigger.flatMapLatest {
        homeUseCase.getPriorityItems(today = LocalDate.now(), dueSoonDays = Constants.DUE_SOON_DAYS)
            .map<List<Item>, AllPrioritiesUiState> { AllPrioritiesUiState.Success(it) }
            .onStart { emit(AllPrioritiesUiState.Loading) }
            .catch { emit(AllPrioritiesUiState.Error(R.string.all_priorities_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), AllPrioritiesUiState.Loading)

    // Mirrors the latest Success list without casting uiState.value (forbidden by mvi.md) —
    // handleComplete needs the domain Item, not just the sealed UI state.
    private var currentItems: List<Item> = emptyList()

    init {
        uiState.onEach { state ->
            if (state is AllPrioritiesUiState.Success) currentItems = state.items
        }.launchIn(viewModelScope)
    }

    private val _uiAction = Channel<AllPrioritiesUiAction>(Channel.BUFFERED)
    val uiAction: Flow<AllPrioritiesUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: AllPrioritiesEvent) {
        when (event) {
            is AllPrioritiesEvent.OnItemClicked -> sendUiAction(AllPrioritiesUiAction.NavigateToDetail(event.itemId))
            is AllPrioritiesEvent.OnCompleteClicked -> handleComplete(event.itemId)
            is AllPrioritiesEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleComplete(itemId: Long) = viewModelScope.launch {
        val item = currentItems.firstOrNull { it.id == itemId } ?: return@launch
        homeUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(AllPrioritiesUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: AllPrioritiesUiAction) = viewModelScope.launch { _uiAction.send(action) }
}
