package com.seucaio.unideas.core.backup.viewmodel

import com.seucaio.unideas.core.backup.domain.model.BackupInfo

sealed interface BackupSyncEvent {
    data class OnDesyncDetected(val remoteBackup: BackupInfo) : BackupSyncEvent
    data object OnRestoreConfirmClicked : BackupSyncEvent
    data object OnRestoreDeclineClicked : BackupSyncEvent
    data object OnDisableSyncConfirmClicked : BackupSyncEvent
    data object OnDisableSyncDeclineClicked : BackupSyncEvent
}
