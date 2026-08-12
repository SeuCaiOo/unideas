package com.seucaio.unideas.feature.home.features.home.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag

/** Screen readiness only — no item data. See [FilterState]/[HomeItemsState] for that. */
sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(val hasAnyItem: Boolean) : HomeUiState

    data class Error(@StringRes val messageRes: Int) : HomeUiState
}

/** One Section's slice of [HomeItemsState.tabItems]. `null` [sectionName] means unsectioned. */
data class ItemSectionGroup(
    val sectionId: Long?,
    val sectionName: String?,
    val items: List<Item>,
    val isPinned: Boolean = false,
)

/** Display mode for [HomeItemsState.tabItems]. */
enum class ItemsViewMode { LIST, GRID }

/**
 * [HomeViewModel.homeMode] — which of the Home screen's two interaction modes is active
 * (TopBar/FAB/list-item rendering all branch on this), not just whether a selection is
 * non-empty. [Selection] with an empty [Selection.selectedItemIds] is a valid, distinct state
 * from [Normal] — "select all" toggling back to zero must not read the same as leaving the mode.
 */
sealed interface HomeMode {

    data object Normal : HomeMode

    data class Selection(val selectedItemIds: Set<Long> = emptySet()) : HomeMode
}

/** [HomeViewModel.filterState] — UI-only, never fails/loads. */
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

/** [HomeViewModel.itemsState] — active tab's list. No load/error of its own. */
data class HomeItemsState(
    val tabItems: List<Item> = emptyList(),
    val groupedTabItems: List<ItemSectionGroup> = emptyList(),
)
