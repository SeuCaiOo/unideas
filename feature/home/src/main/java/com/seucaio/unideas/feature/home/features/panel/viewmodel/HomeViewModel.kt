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
 * Exposes [filterState]/[itemsState]/[uiState] as independent `StateFlow`s instead of one
 * `combine`d state — a tab switch only restarts [itemsState], so [uiState] never re-flashes
 * `Loading` for it. `getPriorityItems` returns the full list uncapped; capping and
 * `showSeeAllButton` happen here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val homeUseCase: HomeUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
) : ViewModel() {

    //region filterState

    private val _filterState = MutableStateFlow(FilterState())
    internal val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    //endregion

    //region itemsState

    // Query failures degrade to an empty list — no error state to retry from.
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

    // handleComplete needs the domain Item, not just itemsState's last value.
    private var currentItems: List<Item> = emptyList()

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
        // Failure just leaves availableSections/availableTags empty.
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
