package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

sealed interface ItemsListEvent {

    data class OnItemClicked(val itemId: Long) : ItemsListEvent

    data object OnAddClicked : ItemsListEvent

    data object OnRetryClicked : ItemsListEvent
}
