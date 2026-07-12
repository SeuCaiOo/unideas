package com.seucaio.unideas.feature.items.features.list.viewmodel

/** One-shot navigation actions for the dev-only item listing screen. */
sealed interface ItemsListUiAction {

    data class NavigateToDetail(val itemId: Long) : ItemsListUiAction

    data object NavigateToForm : ItemsListUiAction
}
