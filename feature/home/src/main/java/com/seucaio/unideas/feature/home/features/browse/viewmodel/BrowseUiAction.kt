package com.seucaio.unideas.feature.home.features.browse.viewmodel

import com.seucaio.unideas.domain.model.ItemType

sealed interface BrowseUiAction {

    data class NavigateToDetail(val itemId: Long) : BrowseUiAction

    data class NavigateToAddItem(val type: ItemType) : BrowseUiAction

    data class ShowError(val message: String) : BrowseUiAction
}
