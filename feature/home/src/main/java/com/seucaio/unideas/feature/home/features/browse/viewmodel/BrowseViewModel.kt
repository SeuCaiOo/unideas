package com.seucaio.unideas.feature.home.features.browse.viewmodel

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
class BrowseViewModel(
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

    val itemsState: StateFlow<BrowseItemsState> =
        combine(itemsFlow, filterState) { tabItems, filter ->
            BrowseItemsState(
                tabItems = tabItems,
                groupedTabItems = tabItems.groupBySection(filter.availableSections),
            )
        }.stateIn(viewModelScope, WhileSubscribed(5_000), BrowseItemsState())

    // handleComplete needs the domain Item, not just itemsState's last value.
    private var currentItems: List<Item> = emptyList()

    //endregion

    //region uiState

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<BrowseUiState> = retryTrigger
        .flatMapLatest {
            homeUseCase.hasAnyItem()
                .map<Boolean, BrowseUiState> { BrowseUiState.Success(hasAnyItem = it) }
                .onStart { emit(BrowseUiState.Loading) }
                .catch { emit(BrowseUiState.Error(R.string.home_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), BrowseUiState.Loading)

    //endregion

    //region one-shot navigation/snackbar events

    private val _uiAction = Channel<BrowseUiAction>(Channel.BUFFERED)
    val uiAction: Flow<BrowseUiAction> = _uiAction.receiveAsFlow()

    //endregion

    init {
        viewModelScope.launch { loadReferenceData() }
        itemsState
            .onEach { state -> currentItems = state.tabItems }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: BrowseEvent) {
        when (event) {
            is BrowseEvent.OnTabChanged -> _filterState.update { it.changeTab(event.type) }
            is BrowseEvent.OnSectionFilterChanged -> _filterState.update { it.sectionFilter(event.sectionId) }
            is BrowseEvent.OnSectionPinToggled -> handleSectionPinToggle(event.sectionId, event.isPinned)
            is BrowseEvent.OnTagFilterToggled -> _filterState.update { it.toggleTag(event.tagId) }
            is BrowseEvent.OnViewModeChanged -> _filterState.update { it.toggleViewMode(event.viewMode) }
            is BrowseEvent.OnItemClicked -> sendUiAction(BrowseUiAction.NavigateToDetail(event.itemId))
            is BrowseEvent.OnCompleteClicked -> handleComplete(event.itemId)
            is BrowseEvent.OnAddClicked -> sendUiAction(BrowseUiAction.NavigateToAddItem(event.type))
            is BrowseEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
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

    private fun handleSectionPinToggle(sectionId: Long, isPinned: Boolean) = viewModelScope.launch {
        homeUseCase.setSectionPinned(sectionId, isPinned)
            .onSuccess { loadReferenceData() }
            .onFailure { sendUiAction(BrowseUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleComplete(itemId: Long) = viewModelScope.launch {
        val item = currentItems.firstOrNull { it.id == itemId } ?: return@launch
        homeUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(BrowseUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: BrowseUiAction) =
        viewModelScope.launch { _uiAction.send(action) }
}
