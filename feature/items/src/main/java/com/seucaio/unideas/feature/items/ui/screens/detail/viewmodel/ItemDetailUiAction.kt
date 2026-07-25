package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.annotation.StringRes

sealed interface ItemDetailUiAction {

    data object NavigateBack : ItemDetailUiAction

    /** Known, localized message (validation). */
    data class ShowSnackbar(@StringRes val messageRes: Int) : ItemDetailUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : ItemDetailUiAction

    data class ShareText(val text: String) : ItemDetailUiAction
}
