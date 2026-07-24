package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

sealed interface ItemsListUiAction {

    data class NavigateToDetail(val itemId: Long) : ItemsListUiAction

    data object NavigateToAddItem : ItemsListUiAction
}
