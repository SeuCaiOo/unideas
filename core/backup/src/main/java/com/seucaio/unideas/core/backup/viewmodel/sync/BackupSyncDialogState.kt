package com.seucaio.unideas.core.backup.viewmodel.sync

import com.seucaio.unideas.core.backup.domain.model.BackupInfo

sealed interface BackupSyncDialogState {
    data object None : BackupSyncDialogState
    data class RestorePrompt(
        val remoteBackup: BackupInfo,
        val isRestoring: Boolean = false,
    ) : BackupSyncDialogState {
        fun restoring(active: Boolean): RestorePrompt = copy(isRestoring = active)
    }
    data class DisableSyncConfirm(val remoteBackup: BackupInfo) : BackupSyncDialogState
}
