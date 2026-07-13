package com.seucaio.unideas.core.backup.viewmodel

import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import java.time.LocalDateTime

sealed interface BackupUiState {
    data object Loading : BackupUiState
    data class Ready(
        val isConnected: Boolean = false,
        val lastBackupAt: LocalDateTime? = null,
        val availableBackups: List<BackupInfo> = emptyList(),
    ) : BackupUiState
}
