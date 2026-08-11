package com.seucaio.unideas.feature.home.features.home.viewmodel

import com.seucaio.unideas.domain.model.ItemType

/** User interactions on the "Todos os itens" tab. */
sealed interface HomeEvent {

    data class OnTabChanged(val type: ItemType) : HomeEvent

    data class OnSectionFilterChanged(val sectionId: Long?) : HomeEvent

    data class OnSectionPinToggled(val sectionId: Long, val isPinned: Boolean) : HomeEvent

    data class OnTagFilterToggled(val tagId: Long) : HomeEvent

    data class OnViewModeChanged(val viewMode: ItemsViewMode) : HomeEvent

    data class OnItemClicked(val itemId: Long) : HomeEvent

    data class OnCompleteClicked(val itemId: Long) : HomeEvent

    data class OnAddClicked(val type: ItemType) : HomeEvent

    data object OnRetryClicked : HomeEvent
}
