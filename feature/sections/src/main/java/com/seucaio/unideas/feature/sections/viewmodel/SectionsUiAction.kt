package com.seucaio.unideas.feature.sections.viewmodel

import androidx.annotation.StringRes

/** One-shot UI actions for the manage-sections screen. */
sealed interface SectionsUiAction {

    /** Known, localized message (validation, blocked deletion). */
    data class ShowSnackbar(@StringRes val messageRes: Int, val formatArgs: List<Any> = emptyList()) : SectionsUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : SectionsUiAction
}
