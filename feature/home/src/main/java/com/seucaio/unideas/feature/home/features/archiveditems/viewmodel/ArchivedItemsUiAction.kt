package com.seucaio.unideas.feature.home.features.archiveditems.viewmodel

sealed interface ArchivedItemsUiAction {

    data class NavigateToDetail(val itemId: Long) : ArchivedItemsUiAction

    data class ShowError(val message: String) : ArchivedItemsUiAction
}
