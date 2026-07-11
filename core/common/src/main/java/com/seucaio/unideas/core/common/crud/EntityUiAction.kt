package com.seucaio.unideas.core.common.crud

import androidx.annotation.StringRes

/** One-shot UI actions for a generic named-entity management screen. */
sealed interface EntityUiAction {

    /** Known, localized message (validation, blocked deletion). */
    data class ShowSnackbar(@StringRes val messageRes: Int, val formatArgs: List<Any> = emptyList()) : EntityUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : EntityUiAction
}
