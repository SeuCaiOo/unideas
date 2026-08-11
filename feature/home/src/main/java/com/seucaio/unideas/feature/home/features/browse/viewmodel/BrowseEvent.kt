package com.seucaio.unideas.feature.home.features.browse.viewmodel

import com.seucaio.unideas.domain.model.ItemType

/** User interactions on the "Todos os itens" tab. */
sealed interface BrowseEvent {

    data class OnTabChanged(val type: ItemType) : BrowseEvent

    data class OnSectionFilterChanged(val sectionId: Long?) : BrowseEvent

    data class OnSectionPinToggled(val sectionId: Long, val isPinned: Boolean) : BrowseEvent

    data class OnTagFilterToggled(val tagId: Long) : BrowseEvent

    data class OnViewModeChanged(val viewMode: ItemsViewMode) : BrowseEvent

    data class OnItemClicked(val itemId: Long) : BrowseEvent

    data class OnCompleteClicked(val itemId: Long) : BrowseEvent

    data class OnAddClicked(val type: ItemType) : BrowseEvent

    data object OnRetryClicked : BrowseEvent
}
