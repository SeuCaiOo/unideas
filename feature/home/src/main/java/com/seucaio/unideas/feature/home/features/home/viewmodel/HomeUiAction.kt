package com.seucaio.unideas.feature.home.features.home.viewmodel

import com.seucaio.unideas.domain.model.ItemType

sealed interface HomeUiAction {

    data class NavigateToDetail(val itemId: Long) : HomeUiAction

    data class NavigateToAddItem(val type: ItemType) : HomeUiAction

    data class ShowError(val message: String) : HomeUiAction
}
