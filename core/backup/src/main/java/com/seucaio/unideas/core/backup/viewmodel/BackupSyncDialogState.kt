package com.seucaio.unideas.core.backup.viewmodel

import com.seucaio.unideas.core.backup.domain.model.BackupInfo

sealed interface BackupSyncDialogState {
    data object None : BackupSyncDialogState
    data class RestorePrompt(val remoteBackup: BackupInfo) : BackupSyncDialogState
    data class DisableSyncConfirm(val remoteBackup: BackupInfo) : BackupSyncDialogState
}
