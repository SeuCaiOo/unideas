package com.seucaio.unideas.feature.home.features.panel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
 * The active tab, section/tag filters, view mode (list/grid) and reference data (available
 * sections/tags, loaded once via [GetSectionsAndTagsUseCase] — they can't change while this
 * screen is open) are real UI-only [InternalState]. [HomeUseCase] is a facade over the
 * single-purpose Item use cases this screen needs — same shape as
 * [com.seucaio.unideas.domain.usecase.item.ItemDetailUseCase]/
 * [com.seucaio.unideas.domain.usecase.item.ItemFormUseCase], named after this screen since Item's
 * use cases split unevenly across screens. `getPriorityItems` returns the full ordered list
 * uncapped; this ViewModel applies the panel's display limit and derives `showSeeAllButton` from
 * whether the list was truncated.
 *
 * [uiState] deliberately does **not** `flatMapLatest` over all of [InternalState] the way
 * [com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel] does (#102,
 * fixed 2026-07-21) — `getPriorityItems`/`hasAnyItem` don't depend on the active tab/filters, so
 * restarting them on every tab switch reissued a screen-wide [HomeUiState.Loading] that hid the
 * priority panel/tabs/filters too, not just the list, and dropped any Compose-local `remember`
 * state (list scroll position, the collapsible priority panel's offset) since the whole
 * `Success` subtree got torn down and rebuilt. Only [homeTabItemsWithState] restarts on tab/filter
 * change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val homeUseCase: HomeUseCase,
    private val getSectionsAndTags: GetSectionsAndTagsUseCase,
) : ViewModel() {

    private data class InternalState(
        val activeTab: ItemType = ItemType.TASK,
        val sectionFilter: Long? = null,
        val tagFilters: Set<Long> = emptySet(),
        val availableSections: List<Section> = emptyList(),
        val availableTags: List<Tag> = emptyList(),
        val viewMode: ItemsViewMode = ItemsViewMode.LIST,
    )

    private val internalState = MutableStateFlow(InternalState())
    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    init {
        viewModelScope.launch { loadReferenceData() }
    }

    private suspend fun loadReferenceData() {
        // Failure here just leaves availableSections/availableTags empty — same silent-degrade
        // rationale as ItemFormViewModel, GetSectionsAndTagsUseCase already falls back on its own.
        runCatching { getSectionsAndTags() }.onSuccess { referenceData ->
            internalState.update {
                it.copy(availableSections = referenceData.sections, availableTags = referenceData.tags)
            }
        }
    }

    /** Only the three keys [homeTabItemsWithState] actually needs to restart [HomeUseCase.getItems] on. */
    private data class TabFilterKey(val activeTab: ItemType, val sectionFilter: Long?, val tagFilters: Set<Long>)

    /**
     * Pairs the current [InternalState] with the tab items for its [TabFilterKey], atomically —
     * combining [internalState] directly *and* a copy of it derived via `flatMapLatest` (keyed on
     * [TabFilterKey]) in the same downstream `combine` would race: on a real key change the two
     * update at different times, so the combined result could briefly show the new `activeTab`
     * paired with the previous tab's items. Combining `internalState` only *inside* the
     * `flatMapLatest` block sidesteps that — a key change restarts this flow and it simply doesn't
     * emit again until the new query resolves, instead of emitting a mismatched pair.
     */
    private val homeTabItemsWithState: Flow<Pair<InternalState, List<Item>>> = internalState
        .map { TabFilterKey(it.activeTab, it.sectionFilter, it.tagFilters) }
        .distinctUntilChanged()
        .flatMapLatest { key ->
            combine(internalState, homeUseCase.getItems(key.activeTab, key.sectionFilter, key.tagFilters.toList())) {
                    internal, items ->
                internal to items
            }
        }

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            combine(
                homeUseCase.getPriorityItems(today = LocalDate.now(), dueSoonDays = Constants.DUE_SOON_DAYS),
                homeUseCase.hasAnyItem(),
                homeTabItemsWithState,
            ) { priorityItems, hasAnyItem, (internal, tabItems) ->
                HomeUiState.Success(
                    priorityItems = priorityItems.take(Constants.PRIORITY_PANEL_LIMIT),
                    showSeeAllButton = priorityItems.size > Constants.PRIORITY_PANEL_LIMIT,
                    activeTab = internal.activeTab,
                    tabItems = tabItems,
                    groupedTabItems = tabItems.groupBySection(internal.availableSections),
                    sectionFilter = internal.sectionFilter,
                    tagFilters = internal.tagFilters,
                    availableSections = internal.availableSections,
                    availableTags = internal.availableTags,
                    hasAnyItem = hasAnyItem,
                    viewMode = internal.viewMode,
                ) as HomeUiState
            }
                .onStart { emit(HomeUiState.Loading) }
                // Caught per-inner-flow (not on the outer chain) so a failure only ends this
                // attempt — retryTrigger stays collected and OnRetryClicked can still restart it.
                .catch { emit(HomeUiState.Error(R.string.home_load_error)) }
        }
        .stateIn(viewModelScope, WhileSubscribed(5_000), HomeUiState.Loading)

    // Mirrors the latest Success lists without casting uiState.value (forbidden by mvi.md) —
    // handleComplete needs the domain Item, not just the sealed UI state.
    private var currentItems: List<Item> = emptyList()

    init {
        uiState.onEach { state ->
            if (state is HomeUiState.Success) currentItems = state.priorityItems + state.tabItems
        }.launchIn(viewModelScope)
    }

    private val _uiAction = Channel<HomeUiAction>(Channel.BUFFERED)
    val uiAction: Flow<HomeUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnTabChanged -> internalState.update { it.copy(activeTab = event.type) }
            is HomeEvent.OnSectionFilterChanged -> internalState.update { it.copy(sectionFilter = event.sectionId) }
            is HomeEvent.OnTagFilterToggled -> internalState.update { it.toggleTag(event.tagId) }
            is HomeEvent.OnViewModeChanged -> internalState.update { it.copy(viewMode = event.viewMode) }
            is HomeEvent.OnItemClicked -> sendUiAction(HomeUiAction.NavigateToDetail(event.itemId))
            is HomeEvent.OnCompleteClicked -> handleComplete(event.itemId)
            is HomeEvent.OnAddClicked -> sendUiAction(HomeUiAction.NavigateToForm(event.type))
            is HomeEvent.OnSeeAllClicked -> sendUiAction(HomeUiAction.NavigateToAllPriorities)
            is HomeEvent.OnSettingsClicked -> sendUiAction(HomeUiAction.NavigateToSettings)
            is HomeEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun InternalState.toggleTag(tagId: Long): InternalState =
        copy(tagFilters = if (tagId in tagFilters) tagFilters - tagId else tagFilters + tagId)

    private fun handleComplete(itemId: Long) = viewModelScope.launch {
        val item = currentItems.firstOrNull { it.id == itemId } ?: return@launch
        homeUseCase.complete(item, LocalDateTime.now())
            .onFailure { sendUiAction(HomeUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: HomeUiAction) = viewModelScope.launch { _uiAction.send(action) }
}
