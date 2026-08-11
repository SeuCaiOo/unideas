package com.seucaio.unideas.feature.home.features.browse.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag

/** Screen readiness only — no item data. See [FilterState]/[BrowseItemsState] for that. */
sealed interface BrowseUiState {

    data object Loading : BrowseUiState

    data class Success(val hasAnyItem: Boolean) : BrowseUiState

    data class Error(@StringRes val messageRes: Int) : BrowseUiState
}

/** One Section's slice of [BrowseItemsState.tabItems]. `null` [sectionName] means unsectioned. */
data class ItemSectionGroup(
    val sectionId: Long?,
    val sectionName: String?,
    val items: List<Item>,
    val isPinned: Boolean = false,
)

/** Display mode for [BrowseItemsState.tabItems]. */
enum class ItemsViewMode { LIST, GRID }

/** [BrowseViewModel.filterState] — UI-only, never fails/loads. */
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

/** [BrowseViewModel.itemsState] — active tab's list. No load/error of its own. */
data class BrowseItemsState(
    val tabItems: List<Item> = emptyList(),
    val groupedTabItems: List<ItemSectionGroup> = emptyList(),
)
