package com.seucaio.unideas.feature.settings.viewmodel

/** UI state for the settings shell. No Loading/Error yet — nothing to load or fail until E1/E2 (Drive backup) exist. */
sealed interface SettingsUiState {

    data class Success(val backupStatus: BackupStatus) : SettingsUiState
}

/** Placeholder until E2 wires the real Google Drive connection state. */
enum class BackupStatus {
    DISCONNECTED,
}
