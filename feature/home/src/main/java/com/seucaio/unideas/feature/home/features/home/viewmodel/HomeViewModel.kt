package com.seucaio.unideas.feature.home.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.usecase.SectionsAndTagsUseCase
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val homeUseCase: HomeUseCase,
    private val sectionsAndTagsUseCase: SectionsAndTagsUseCase,
) : ViewModel() {

    //region filterState

    private val _filterState = MutableStateFlow(FilterState())
    internal val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val referenceDataFlow = sectionsAndTagsUseCase.getAll()

    //endregion

    //region itemsState

    // Query failures degrade to an empty list — no error state to retry from.
    private val itemsFlow: Flow<List<Item>> = filterState
        .distinctUntilChangedBy { Triple(it.activeTab, it.sectionFilter, it.tagFilters) }
        .flatMapLatest { filter ->
            homeUseCase.getItems(filter.activeTab, filter.sectionFilter, filter.tagFilters.toList())
                .catch { emit(emptyList()) }
        }

    val itemsState: StateFlow<HomeItemsState> =
        combine(itemsFlow, filterState) { tabItems, filter ->
            HomeItemsState(
                tabItems = tabItems,
                groupedTabItems = tabItems.groupBySection(filter.availableSections),
                isLoaded = true,
            )
        }.stateIn(viewModelScope, WhileSubscribed(5_000), HomeItemsState())

    // handleComplete needs the domain Item, not just itemsState's last value.
    private var currentItems: List<Item> = emptyList()

    //endregion

    //region homeMode

    private val _homeMode = MutableStateFlow<HomeMode>(HomeMode.Normal)
    val homeMode: StateFlow<HomeMode> = _homeMode.asStateFlow()

    private val _dialogState = MutableStateFlow<HomeDialogState>(HomeDialogState.None)
    val dialogState: StateFlow<HomeDialogState> = _dialogState.asStateFlow()

    //endregion

    //region uiState

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val hasAnyPriorityItemFlow: Flow<Boolean> =
        homeUseCase.getPriorityItems(today = LocalDate.now(), dueSoonDays = Constants.DUE_SOON_DAYS)
            .map { it.isNotEmpty() }
            .catch { emit(false) }

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            combine(
                homeUseCase.hasAnyItem(),
                hasAnyPriorityItemFlow,
                itemsState
            ) { hasAnyItem, hasAnyPriorityItem, items ->
                if (items.isLoaded) {
                    HomeUiState.Success(
                        hasAnyItem = hasAnyItem,
                        hasAnyPriorityItem = hasAnyPriorityItem
                    )
                } else {
                    HomeUiState.Loading
                }
            }.catch { emit(HomeUiState.Error(R.string.home_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), HomeUiState.Loading)

    //endregion

    //region pull-to-refresh

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    //endregion

    //region one-shot navigation/snackbar events

    private val _uiAction = Channel<HomeUiAction>(Channel.BUFFERED)
    val uiAction: Flow<HomeUiAction> = _uiAction.receiveAsFlow()

    //endregion

    init {
        referenceDataFlow
            .onEach { (sections, tags) -> _filterState.update { it.setFilters(sections, tags) } }
            .launchIn(viewModelScope)
        itemsState
            .onEach { state -> currentItems = state.tabItems }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnTabChanged -> _filterState.update { it.changeTab(event.type) }
            is HomeEvent.OnSectionFilterChanged -> _filterState.update { it.sectionFilter(event.sectionId) }
            is HomeEvent.OnSectionPinToggled -> handleSectionPinToggle(event.sectionId, event.isPinned)
            is HomeEvent.OnItemPinToggled -> handleItemPinToggle(event.itemId, event.isPinned)
            is HomeEvent.OnTagFilterToggled -> _filterState.update { it.toggleTag(event.tagId) }
            is HomeEvent.OnViewModeChanged -> _filterState.update { it.toggleViewMode(event.viewMode) }
            is HomeEvent.OnItemClicked -> handleItemClicked(event.itemId)
            is HomeEvent.OnCompleteClicked -> handleComplete(event.itemId)
            is HomeEvent.OnAddClicked -> sendUiAction(HomeUiAction.NavigateToAddItem(event.type))
            is HomeEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
            is HomeEvent.OnRefreshRequested -> handleRefresh()
            is HomeEvent.SelectionEvent -> handleSelectionEvent(event)
        }
    }

    private fun handleSelectionEvent(event: HomeEvent.SelectionEvent) {
        when (event) {
            is HomeEvent.OnItemLongPressed -> toggleSelection(event.itemId)
            is HomeEvent.OnItemSelectionToggled -> toggleSelection(event.itemId)
            is HomeEvent.OnSelectionCleared -> _homeMode.value = HomeMode.Normal
            is HomeEvent.OnDeleteSelectedClicked -> _dialogState.value = HomeDialogState.DeleteSelectedConfirm
            is HomeEvent.OnDeleteSelectedConfirmClicked -> {
                _dialogState.value = HomeDialogState.None
                handleDeleteSelected()
            }
            is HomeEvent.OnDeleteDialogDismissed -> _dialogState.value = HomeDialogState.None
            is HomeEvent.OnSelectAllClicked -> toggleSelectAll(currentItems.map { it.id }.toSet())
            is HomeEvent.OnGroupSelectAllClicked -> toggleSelectAll(
                currentItems.filter { it.sectionId == event.sectionId }.map { it.id }.toSet()
            )
        }
    }

    private fun handleItemClicked(itemId: Long) {
        when (_homeMode.value) {
            is HomeMode.Selection -> toggleSelection(itemId)
            HomeMode.Normal -> sendUiAction(HomeUiAction.NavigateToDetail(itemId))
        }
    }

    private fun toggleSelection(itemId: Long) {
        val current = (_homeMode.value as? HomeMode.Selection)?.selectedItemIds ?: emptySet()
        _homeMode.value = HomeMode.Selection(if (itemId in current) current - itemId else current + itemId)
    }

    private fun toggleSelectAll(ids: Set<Long>) {
        val current = (_homeMode.value as? HomeMode.Selection)?.selectedItemIds ?: return
        if (ids.isEmpty()) return
        val allSelected = ids.all { it in current }
        _homeMode.value = HomeMode.Selection(if (allSelected) current - ids else current + ids)
    }

    private fun handleDeleteSelected() = viewModelScope.launch {
        val mode = _homeMode.value as? HomeMode.Selection ?: return@launch
        homeUseCase.deleteItems(mode.selectedItemIds.toList())
            .onSuccess { _homeMode.value = HomeMode.Normal }
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleSectionPinToggle(sectionId: Long, isPinned: Boolean) = viewModelScope.launch {
        homeUseCase.setSectionPinned(sectionId, isPinned)
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleItemPinToggle(itemId: Long, isPinned: Boolean) = viewModelScope.launch {
        homeUseCase.setItemPinned(itemId, isPinned)
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleRefresh() = viewModelScope.launch {
        _isRefreshing.value = true
        homeUseCase.refreshReminders()
        _isRefreshing.value = false
    }

    private fun handleComplete(itemId: Long) = viewModelScope.launch {
        val item = currentItems.firstOrNull { it.id == itemId } ?: return@launch
        homeUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: HomeUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
