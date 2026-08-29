package com.seucaio.unideas.core.backup.viewmodel

import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import java.time.LocalDateTime

sealed interface BackupUiState {
    data object Loading : BackupUiState
    data class Ready(
        val isConnected: Boolean = false,
        val lastBackupAt: LocalDateTime? = null,
        val isBackupListVisible: Boolean = false,
        val backupListStatus: BackupListStatus = BackupListStatus.Empty,
        val selectedBackupFileId: String? = null,
        val isAutoBackupEnabled: Boolean = false,
    ) : BackupUiState
}

sealed interface BackupListStatus {
    data object Empty : BackupListStatus
    data object Error : BackupListStatus
    data class Loaded(val backups: List<BackupListEntry>) : BackupListStatus
}

data class BackupListEntry(val info: BackupInfo, val isAutomatic: Boolean = false)
