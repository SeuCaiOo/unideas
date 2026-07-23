package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

/** User interactions on the dev-only item listing screen. */
sealed interface ItemsListEvent {

    data class OnItemClicked(val itemId: Long) : ItemsListEvent

    data object OnAddClicked : ItemsListEvent

    data object OnRetryClicked : ItemsListEvent
}
