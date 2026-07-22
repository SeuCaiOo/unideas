package com.seucaio.unideas.feature.home.features.panel.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag

/**
 * Screen-readiness state for the Home priority panel + tabs + filters screen — deliberately scoped
 * to just "can the screen render / did it fail to load" (#102, 2026-07-22). No item data lives
 * here: [FilterState] (UI-only, never fails) and [ItemsState] (query results, no load/error of
 * their own — a query either produced a list or, on failure, silently degrades to an empty one)
 * are collected as their own `StateFlow`s by
 * [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen]/
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen`, independent of this one.
 * [Success.hasAnyItem] survives here because it's the one item-adjacent signal the *screen* itself
 * needs — whether to render the true first-run empty state at all — not a concern of [ItemsState].
 */
sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(val hasAnyItem: Boolean) : HomeUiState

    data class Error(@StringRes val messageRes: Int) : HomeUiState
}

/**
 * One Section's slice of [ItemsState.tabItems]. [sectionName] `null` means the unsectioned bucket
 * (items with no [Item.sectionId]) — resolved to a localized label at the Composable layer, not
 * here (this class stays domain-agnostic like the rest of this file).
 */
data class ItemSectionGroup(
    val sectionId: Long?,
    val sectionName: String?,
    val items: List<Item>,
)

/**
 * Display mode for [ItemsState.tabItems] — a presentation-only choice (via
 * [com.seucaio.unideas.ds.components.buttons.ViewModeToggleButton] at the call site), does not
 * touch grouping/filtering. [LIST] and [GRID] are equal siblings — the user picks one, neither
 * replaces the other.
 */
enum class ItemsViewMode { LIST, GRID }

/**
 * [HomeViewModel.filterState] — UI-only: active tab, section/tag filters, reference data
 * (available sections/tags), view mode. Never fails, never loads — always has a value.
 */
internal data class FilterState(
    val activeTab: ItemType = ItemType.TASK,
    val sectionFilter: Long? = null,
    val tagFilters: Set<Long> = emptySet(),
    val availableSections: List<Section> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val viewMode: ItemsViewMode = ItemsViewMode.LIST,
) {
    fun toggleTag(tagId: Long): FilterState {
        val isTagSelected = tagId in tagFilters
        return copy(tagFilters = if (isTagSelected) tagFilters - tagId else tagFilters + tagId)
    }

    fun toggleViewMode(viewMode: ItemsViewMode): FilterState = copy(viewMode = viewMode)

    fun sectionFilter(sectionId: Long?): FilterState = copy(sectionFilter = sectionId)

    fun changeTab(type: ItemType): FilterState = copy(activeTab = type)

    fun setFilters(sections: List<Section>, tags: List<Tag>): FilterState =
        copy(availableSections = sections, availableTags = tags)
}

/**
 * [HomeViewModel.itemsState] — priority panel + active tab's list, together since both come from
 * the same item queries. No load/error of its own (see [HomeUiState] doc) — a query failure just
 * leaves the previous (or default empty) value in place.
 */
data class ItemsState(
    val priorityItems: List<Item> = emptyList(),
    val showSeeAllButton: Boolean = false,
    val tabItems: List<Item> = emptyList(),
    val groupedTabItems: List<ItemSectionGroup> = emptyList(),
)
