package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.annotation.StringRes

/** One-shot UI actions for the create/edit item form. */
sealed interface ItemDetailUiAction {

    /** Saved successfully — the Screen should pop the back stack. */
    data object NavigateBack : ItemDetailUiAction

    /** Known, localized message (validation). */
    data class ShowSnackbar(@StringRes val messageRes: Int) : ItemDetailUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : ItemDetailUiAction

    /** Launch the system share sheet with [text]. */
    data class ShareText(val text: String) : ItemDetailUiAction
}
