package com.seucaio.unideas.feature.home.features.panel.viewmodel

import com.seucaio.unideas.domain.model.ItemType

/** One-shot UI actions for the Home priority panel screen. */
sealed interface HomeUiAction {

    data class NavigateToDetail(val itemId: Long) : HomeUiAction

    data class NavigateToForm(val type: ItemType) : HomeUiAction

    data object NavigateToAllPriorities : HomeUiAction

    data object NavigateToSettings : HomeUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : HomeUiAction
}
