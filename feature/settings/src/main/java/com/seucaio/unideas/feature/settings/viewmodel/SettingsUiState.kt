package com.seucaio.unideas.feature.settings.viewmodel

/**
 * UI state for the settings shell. No Loading/Error yet — nothing to load or fail here; the
 * real Backup connection state lives in `BackupViewModel` (`:core:backup`), collected directly
 * by `SettingsScreen` alongside this one.
 */
sealed interface SettingsUiState {

    data object Success : SettingsUiState
}
