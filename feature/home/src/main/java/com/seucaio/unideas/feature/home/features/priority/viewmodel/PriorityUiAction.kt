package com.seucaio.unideas.feature.home.features.priority.viewmodel

sealed interface PriorityUiAction {

    data class NavigateToDetail(val itemId: Long) : PriorityUiAction

    data object NavigateToAllPriorities : PriorityUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : PriorityUiAction
}
