package com.seucaio.unideas.feature.home.features.panel.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag

/**
 * UI state for the Home priority panel + tabs + filters screen.
 *
 * Unlike the item form ([com.seucaio.unideas.feature.items] exception 2), Home is genuinely
 * reactive — the panel and the active tab's list can each fail or become empty independently
 * at any time — so it keeps the traditional `Loading`/`Success`/`Error` shape.
 */
sealed interface HomeUiState {

    data object Loading : HomeUiState

    /**
     * @property priorityItems fixed panel content (overdue + due-soon), independent of [activeTab].
     * @property showSeeAllButton true when [priorityItems] was truncated to fit the panel's limit.
     * @property tabItems the active tab's list, filtered by [sectionFilter]/[tagFilters].
     * @property groupedTabItems [tabItems] grouped by Section, in [availableSections] order, with
     *   an unsectioned bucket ([ItemSectionGroup.sectionName] `null`) last if present. Only
     *   meaningful (and only rendered as such) when [sectionFilter] is `null` — a single-section
     *   filter already narrows the list to one section, so grouping chrome would be redundant.
     * @property hasAnyItem false only when the user has never created an item anywhere in the
     *   app — distinguishes the true first-run empty state from [tabItems] just being empty
     *   because of the active tab/filters.
     * @property viewMode list vs. grid presentation. Lives here (not local `remember` state in
     *   the Composable) because [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen]
     *   and `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen` are screens
     *   sharing this same ViewModel, not a component — same rationale as [activeTab]/
     *   [sectionFilter]/[tagFilters] already being here instead of per-screen state.
     */
    data class Success(
        val priorityItems: List<Item>,
        val showSeeAllButton: Boolean,
        val activeTab: ItemType,
        val tabItems: List<Item>,
        val groupedTabItems: List<ItemSectionGroup>,
        val sectionFilter: Long?,
        val tagFilters: Set<Long>,
        val availableSections: List<Section>,
        val availableTags: List<Tag>,
        val hasAnyItem: Boolean,
        val viewMode: ItemsViewMode,
    ) : HomeUiState

    data class Error(@StringRes val messageRes: Int) : HomeUiState
}

/**
 * One Section's slice of [HomeUiState.Success.tabItems]. [sectionName] `null` means the
 * unsectioned bucket (items with no [Item.sectionId]) — resolved to a localized label at the
 * Composable layer, not here (this class stays domain-agnostic like the rest of `HomeUiState`).
 */
data class ItemSectionGroup(
    val sectionId: Long?,
    val sectionName: String?,
    val items: List<Item>,
)

/**
 * Display mode for [HomeUiState.Success.tabItems] — a presentation-only choice (via
 * [com.seucaio.unideas.ds.components.buttons.ViewModeToggleButton] at the call site), does not
 * touch grouping/filtering. [LIST] and [GRID] are equal siblings — the user picks one, neither
 * replaces the other.
 */
enum class ItemsViewMode { LIST, GRID }
