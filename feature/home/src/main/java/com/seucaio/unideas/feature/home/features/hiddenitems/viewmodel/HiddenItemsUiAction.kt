package com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel

sealed interface HiddenItemsUiAction {

    data class NavigateToDetail(val itemId: Long) : HiddenItemsUiAction

    data class ShowError(val message: String) : HiddenItemsUiAction
}
