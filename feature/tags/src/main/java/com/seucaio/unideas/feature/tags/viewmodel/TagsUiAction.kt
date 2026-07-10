package com.seucaio.unideas.feature.tags.viewmodel

import androidx.annotation.StringRes

/** One-shot UI actions for the manage-tags screen. */
sealed interface TagsUiAction {

    /** Known, localized message (validation, blocked deletion). */
    data class ShowSnackbar(@StringRes val messageRes: Int, val formatArgs: List<Any> = emptyList()) : TagsUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : TagsUiAction
}
