package com.seucaio.unideas.feature.settings.viewmodel

import androidx.annotation.StringRes

/** One-shot actions for the settings shell — navigation and debug-seed feedback. */
sealed interface SettingsUiAction {

    data object NavigateToSections : SettingsUiAction

    data object NavigateToTags : SettingsUiAction

    /** Temporary until Home (#27) ships as the real entry point to Items. */
    data object NavigateToItems : SettingsUiAction

    /** Sent after a successful debug seed/clear — goes back to Home so the result is visible right away. */
    data object NavigateBack : SettingsUiAction

    /** Known, localized message (debug-seed success). */
    data class ShowSnackbar(@StringRes val messageRes: Int) : SettingsUiAction

    /** Unexpected failure — raw exception message, not localized. */
    data class ShowError(val message: String) : SettingsUiAction
}
