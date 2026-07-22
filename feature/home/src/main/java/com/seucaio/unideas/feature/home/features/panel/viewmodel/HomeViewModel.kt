package com.seucaio.unideas.feature.home.features.panel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.HomeUseCase
import com.seucaio.unideas.feature.home.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel for the Home priority panel + tabs + filters screen.
 *
 * Three independent `StateFlow`s (#102, 2026-07-22) instead of one `combine`d [HomeUiState], each
 * collected directly by [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen] /
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen`:
 * - [filterState]: activeTab, section/tag filters, reference data, view mode — real UI-only
 *   state, never fails/loads, mutated directly by [onEvent].
 * - [itemsState]: priority panel + active tab's list — query results, no load/error of their own;
 *   a failure just degrades to an empty list, same rationale as [loadReferenceData].
 * - [uiState] ([HomeUiState]): the only place that knows about Loading/Error, scoped to just
 *   `hasAnyItem` — it has no item data at all, so it needs no `combine()` to build itself.
 *
 * Switching tabs restarts only [itemsState]; [uiState] never reloads for that — the original bug
 * this replaced was a single `flatMapLatest` over one `InternalState` blob, which reissued a
 * screen-wide `Loading` on every tab/filter change (hid the priority panel/tabs/filters too, not
 * just the list, and dropped Compose-local `remember` state like scroll position). Splitting the
 * concerns by *who writes them and when* — [filterState] is user-event-driven, [itemsState] is
 * query-derived, [uiState] is screen-readiness-derived — removes the need for that blob and the
 * race-avoidance tricks it required.
 *
 * [HomeUseCase] is a facade over the single-purpose Item use cases this screen needs — same shape
 * as [com.seucaio.unideas.domain.usecase.item.ItemDetailUseCase]/
 * [com.seucaio.unideas.domain.usecase.item.ItemFormUseCase]. `getPriorityItems` returns the full
 * ordered list uncapped; this ViewModel applies the panel's display limit and derives
 * `showSeeAllButton` from whether the list was truncated.
 *
 * Member order follows the Kotlin coding conventions (properties/`init` first, grouped by what
 * each piece feeds into; methods last) — https://kotlinlang.org/docs/coding-conventions.html#class-layout.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val homeUseCase: HomeUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
) : ViewModel() {

    //region filterState — UI-only, user-event-driven (see onEvent)

    private val _filterState = MutableStateFlow(FilterState())
    internal val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    //endregion

    //region itemsState — derived from filterState + the item queries, no load/error of its own

    // No load/error here — a query failure just degrades to an empty list, same rationale as
    // loadReferenceData() below. There's nothing for OnRetryClicked to restart: an empty result
    // isn't an error state to recover from, it's just what "no data (yet or ever)" looks like.
    // Restart the query only on the three fields it actually depends on — no need for a bespoke
    // key type, Triple already has structural equality for distinctUntilChangedBy.
    private val itemsFlow: Flow<List<Item>> = filterState
        .distinctUntilChangedBy { Triple(it.activeTab, it.sectionFilter, it.tagFilters) }
        .flatMapLatest { filter ->
            homeUseCase.getItems(filter.activeTab, filter.sectionFilter, filter.tagFilters.toList())
                .catch { emit(emptyList()) }
        }

    private val priorityFlow: Flow<List<Item>> =
        homeUseCase.getPriorityItems(today = LocalDate.now(), dueSoonDays = Constants.DUE_SOON_DAYS)
            .catch { emit(emptyList()) }

    val itemsState: StateFlow<ItemsState> =
        combine(priorityFlow, itemsFlow, filterState) { priorityItems, tabItems, filter ->
            ItemsState(
                priorityItems = priorityItems.take(Constants.PRIORITY_PANEL_LIMIT),
                showSeeAllButton = priorityItems.size > Constants.PRIORITY_PANEL_LIMIT,
                tabItems = tabItems,
                groupedTabItems = tabItems.groupBySection(filter.availableSections),
            )
        }.stateIn(viewModelScope, WhileSubscribed(5_000), ItemsState())

    // Mirrors the latest itemsState lists without casting itemsState.value (forbidden by
    // mvi.md's spirit) — handleComplete needs the domain Item. Kept subscribed via init below.
    private var currentItems: List<Item> = emptyList()

    //endregion

    //region uiState — screen readiness only (Loading/Success/Error)

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // Scoped to just "can the screen render" — no item data, so no combine() needed to build it.
    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            homeUseCase.hasAnyItem()
                .map<Boolean, HomeUiState> { HomeUiState.Success(hasAnyItem = it) }
                .onStart { emit(HomeUiState.Loading) }
                // Caught per-inner-flow (not on the outer chain) so a failure only ends this
                // attempt — retryTrigger stays collected and OnRetryClicked can still restart it.
                .catch { emit(HomeUiState.Error(R.string.home_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), HomeUiState.Loading)

    //endregion

    //region one-shot navigation/snackbar events

    private val _uiAction = Channel<HomeUiAction>(Channel.BUFFERED)
    val uiAction: Flow<HomeUiAction> = _uiAction.receiveAsFlow()

    //endregion

    init {
        viewModelScope.launch { loadReferenceData() }
        itemsState
            .onEach { state -> currentItems = state.priorityItems + state.tabItems }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnTabChanged -> _filterState.update { it.changeTab(event.type) }
            is HomeEvent.OnSectionFilterChanged -> _filterState.update { it.sectionFilter(event.sectionId) }
            is HomeEvent.OnTagFilterToggled -> _filterState.update { it.toggleTag(event.tagId) }
            is HomeEvent.OnViewModeChanged -> _filterState.update { it.toggleViewMode(event.viewMode) }
            is HomeEvent.OnItemClicked -> sendUiAction(HomeUiAction.NavigateToDetail(event.itemId))
            is HomeEvent.OnCompleteClicked -> handleComplete(event.itemId)
            is HomeEvent.OnAddClicked -> sendUiAction(HomeUiAction.NavigateToForm(event.type))
            is HomeEvent.OnSeeAllClicked -> sendUiAction(HomeUiAction.NavigateToAllPriorities)
            is HomeEvent.OnSettingsClicked -> sendUiAction(HomeUiAction.NavigateToSettings)
            is HomeEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private suspend fun loadReferenceData() {
        // Failure here just leaves availableSections/availableTags empty — same silent-degrade
        // rationale as ItemFormViewModel, GetSectionsAndTagsUseCase already falls back on its own.
        runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
            _filterState.update {
                it.setFilters(sections = referenceData.sections, tags = referenceData.tags)
            }
        }
    }

    private fun handleComplete(itemId: Long) = viewModelScope.launch {
        val item = currentItems.firstOrNull { it.id == itemId } ?: return@launch
        homeUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: HomeUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
