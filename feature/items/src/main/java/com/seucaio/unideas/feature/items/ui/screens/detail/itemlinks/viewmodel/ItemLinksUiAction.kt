package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel

sealed interface ItemLinksUiAction {
    data class NavigateToDetail(val itemId: Long) : ItemLinksUiAction
    data class ShowError(val message: String) : ItemLinksUiAction
}
