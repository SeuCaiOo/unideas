package com.seucaio.unideas.feature.home.features.panel.viewmodel

import com.seucaio.unideas.domain.model.ItemType

/** User interactions on the Home priority panel screen. */
sealed interface HomeEvent {

    data class OnTabChanged(val type: ItemType) : HomeEvent

    data class OnSectionFilterChanged(val sectionId: Long?) : HomeEvent

    data class OnTagFilterToggled(val tagId: Long) : HomeEvent

    data class OnItemClicked(val itemId: Long) : HomeEvent

    data class OnCompleteClicked(val itemId: Long) : HomeEvent

    data class OnAddClicked(val type: ItemType) : HomeEvent

    data object OnSeeAllClicked : HomeEvent

    data object OnSettingsClicked : HomeEvent

    data object OnRetryClicked : HomeEvent
}
