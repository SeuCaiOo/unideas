package com.seucaio.unideas.feature.home.features.archiveditems.viewmodel

sealed interface ArchivedItemsEvent {

    data class OnItemClicked(val itemId: Long) : ArchivedItemsEvent

    data object OnRetryClicked : ArchivedItemsEvent
}
