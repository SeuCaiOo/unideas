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
     */
    data class Success(
        val priorityItems: List<Item>,
        val showSeeAllButton: Boolean,
        val activeTab: ItemType,
        val tabItems: List<Item>,
        val sectionFilter: Long?,
        val tagFilters: Set<Long>,
        val availableSections: List<Section>,
        val availableTags: List<Tag>,
    ) : HomeUiState

    data class Error(@StringRes val messageRes: Int) : HomeUiState
}
