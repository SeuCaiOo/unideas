package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel

sealed interface ItemLinksEvent {
    data class OnLinkedItemClicked(val itemId: Long) : ItemLinksEvent
    data class OnUnlinkClicked(val itemId: Long) : ItemLinksEvent
    data object OnRetryClicked : ItemLinksEvent
}
