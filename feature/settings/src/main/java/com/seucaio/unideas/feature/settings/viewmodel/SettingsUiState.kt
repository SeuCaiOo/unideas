package com.seucaio.unideas.feature.settings.viewmodel

sealed interface SettingsUiState {

    data object Success : SettingsUiState
}

data class SettingsAccountUiState(
    val isConnected: Boolean = false,
    val accountName: String? = null,
    val accountEmail: String? = null,
)
