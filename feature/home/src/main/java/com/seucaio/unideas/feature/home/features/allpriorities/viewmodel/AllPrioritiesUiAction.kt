package com.seucaio.unideas.feature.home.features.allpriorities.viewmodel

/** One-shot UI actions for the all-priorities screen. */
sealed interface AllPrioritiesUiAction {

    data class NavigateToDetail(val itemId: Long) : AllPrioritiesUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : AllPrioritiesUiAction
}
