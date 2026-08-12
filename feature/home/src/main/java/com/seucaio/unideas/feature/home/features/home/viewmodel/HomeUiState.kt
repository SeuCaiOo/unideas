package com.seucaio.unideas.feature.home.features.home.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(val hasAnyItem: Boolean) : HomeUiState

    data class Error(@param:StringRes val messageRes: Int) : HomeUiState
}

data class ItemSectionGroup(
    val sectionId: Long?,
    val sectionName: String?,
    val items: List<Item>,
    val isPinned: Boolean = false,
    val isPinnedItemsGroup: Boolean = false,
)

enum class ItemsViewMode { LIST, GRID }

sealed interface HomeMode {

    data object Normal : HomeMode

    data class Selection(val selectedItemIds: Set<Long> = emptySet()) : HomeMode
}

sealed interface HomeDialogState {

    data object None : HomeDialogState

    data object DeleteSelectedConfirm : HomeDialogState
}

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

data class HomeItemsState(
    val tabItems: List<Item> = emptyList(),
    val groupedTabItems: List<ItemSectionGroup> = emptyList(),
    val isLoaded: Boolean = false,
)
