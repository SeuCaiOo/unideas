package com.seucaio.unideas.feature.items.features.form.viewmodel

import androidx.annotation.StringRes

/** One-shot UI actions for the create/edit item form. */
sealed interface ItemFormUiAction {

    /** Saved successfully — the Screen should pop the back stack. */
    data object NavigateBack : ItemFormUiAction

    /** Known, localized message (validation). */
    data class ShowSnackbar(@StringRes val messageRes: Int) : ItemFormUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : ItemFormUiAction

    /** Launch the system share sheet with [text]. */
    data class ShareText(val text: String) : ItemFormUiAction
}
