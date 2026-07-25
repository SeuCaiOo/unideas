package com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel

import androidx.annotation.StringRes

sealed interface AddItemUiAction {

    data object NavigateBack : AddItemUiAction

    /** Known, localized message (validation). */
    data class ShowSnackbar(@StringRes val messageRes: Int) : AddItemUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : AddItemUiAction
}
