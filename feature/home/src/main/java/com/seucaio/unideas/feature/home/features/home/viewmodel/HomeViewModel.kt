package com.seucaio.unideas.feature.home.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.LocalDateTime

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

    val itemsState: StateFlow<HomeItemsState> =
        combine(itemsFlow, filterState) { tabItems, filter ->
            HomeItemsState(
                tabItems = tabItems,
                groupedTabItems = tabItems.groupBySection(filter.availableSections),
            )
        }.stateIn(viewModelScope, WhileSubscribed(5_000), HomeItemsState())

    // handleComplete needs the domain Item, not just itemsState's last value.
    private var currentItems: List<Item> = emptyList()

    //endregion

    //region selection mode

    // isSelectionMode is tracked separately from selectedItemIds — "select all" toggling back to
    // zero selected must not read the same as pressing X (OnSelectionCleared), which is the only
    // event that actually exits the mode.
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedItemIds = MutableStateFlow(emptySet<Long>())
    val selectedItemIds: StateFlow<Set<Long>> = _selectedItemIds.asStateFlow()

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
            .onEach { state -> currentItems = state.tabItems }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnTabChanged -> _filterState.update { it.changeTab(event.type) }
            is HomeEvent.OnSectionFilterChanged -> _filterState.update { it.sectionFilter(event.sectionId) }
            is HomeEvent.OnSectionPinToggled -> handleSectionPinToggle(event.sectionId, event.isPinned)
            is HomeEvent.OnTagFilterToggled -> _filterState.update { it.toggleTag(event.tagId) }
            is HomeEvent.OnViewModeChanged -> _filterState.update { it.toggleViewMode(event.viewMode) }
            is HomeEvent.OnItemClicked -> handleItemClicked(event.itemId)
            is HomeEvent.OnCompleteClicked -> handleComplete(event.itemId)
            is HomeEvent.OnAddClicked -> sendUiAction(HomeUiAction.NavigateToAddItem(event.type))
            is HomeEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
            is HomeEvent.SelectionEvent -> handleSelectionEvent(event)
        }
    }

    private fun handleSelectionEvent(event: HomeEvent.SelectionEvent) {
        when (event) {
            is HomeEvent.OnItemLongPressed -> {
                _isSelectionMode.value = true
                toggleSelection(event.itemId)
            }
            is HomeEvent.OnItemSelectionToggled -> toggleSelection(event.itemId)
            is HomeEvent.OnSelectionCleared -> {
                _isSelectionMode.value = false
                _selectedItemIds.value = emptySet()
            }
            is HomeEvent.OnDeleteSelectedClicked -> handleDeleteSelected()
            is HomeEvent.OnSelectAllClicked -> {
                val allIds = currentItems.map { it.id }.toSet()
                _selectedItemIds.value = if (_selectedItemIds.value == allIds) emptySet() else allIds
            }
        }
    }

    private fun handleItemClicked(itemId: Long) {
        if (_isSelectionMode.value) {
            toggleSelection(itemId)
        } else {
            sendUiAction(HomeUiAction.NavigateToDetail(itemId))
        }
    }

    private fun toggleSelection(itemId: Long) {
        _selectedItemIds.update { current ->
            if (itemId in current) current - itemId else current + itemId
        }
    }

    private fun handleDeleteSelected() = viewModelScope.launch {
        val ids = _selectedItemIds.value.toList()
        homeUseCase.deleteItems(ids)
            .onSuccess {
                _isSelectionMode.value = false
                _selectedItemIds.value = emptySet()
            }
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private suspend fun loadReferenceData() {
        // Failure just leaves availableSections/availableTags empty.
        runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
            _filterState.update {
                it.setFilters(sections = referenceData.sections, tags = referenceData.tags)
            }
        }
    }

    private fun handleSectionPinToggle(sectionId: Long, isPinned: Boolean) = viewModelScope.launch {
        homeUseCase.setSectionPinned(sectionId, isPinned)
            .onSuccess { loadReferenceData() }
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleComplete(itemId: Long) = viewModelScope.launch {
        val item = currentItems.firstOrNull { it.id == itemId } ?: return@launch
        homeUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: HomeUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
