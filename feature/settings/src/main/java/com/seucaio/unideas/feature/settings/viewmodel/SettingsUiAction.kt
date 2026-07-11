package com.seucaio.unideas.feature.settings.viewmodel

/** One-shot navigation actions for the settings shell. */
sealed interface SettingsUiAction {

    data object NavigateToSections : SettingsUiAction

    data object NavigateToTags : SettingsUiAction

    /** Temporary until Home (#27) ships as the real entry point to Items. */
    data object NavigateToItems : SettingsUiAction
}
