package com.seucaio.unideas.feature.home.features.home.viewmodel

import com.seucaio.unideas.domain.model.ItemType

sealed interface HomeEvent {

    //region normal events

    data class OnTabChanged(val type: ItemType) : HomeEvent

    data class OnSectionFilterChanged(val sectionId: Long?) : HomeEvent

    data class OnSectionPinToggled(val sectionId: Long, val isPinned: Boolean) : HomeEvent

    data class OnTagFilterToggled(val tagId: Long) : HomeEvent

    data class OnViewModeChanged(val viewMode: ItemsViewMode) : HomeEvent

    data class OnItemClicked(val itemId: Long) : HomeEvent

    data class OnCompleteClicked(val itemId: Long) : HomeEvent

    data class OnAddClicked(val type: ItemType) : HomeEvent

    data object OnRetryClicked : HomeEvent

    //endregion

    //region selection events

    sealed interface SelectionEvent : HomeEvent

    data class OnItemLongPressed(val itemId: Long) : SelectionEvent

    data class OnItemSelectionToggled(val itemId: Long) : SelectionEvent

    data object OnSelectionCleared : SelectionEvent

    data object OnDeleteSelectedClicked : SelectionEvent

    //endregion
}
