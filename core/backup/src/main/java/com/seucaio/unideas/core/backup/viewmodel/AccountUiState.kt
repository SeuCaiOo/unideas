package com.seucaio.unideas.core.backup.viewmodel

sealed interface AccountUiState {
    data class Ready(
        val isConnected: Boolean = false,
        val accountName: String? = null,
        val accountEmail: String? = null,
    ) : AccountUiState
}
