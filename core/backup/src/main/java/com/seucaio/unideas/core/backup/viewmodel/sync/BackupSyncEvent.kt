package com.seucaio.unideas.core.backup.viewmodel.sync

sealed interface BackupSyncEvent {
    data object OnSyncCheckRequested : BackupSyncEvent
    data object OnRestoreConfirmClicked : BackupSyncEvent
    data object OnRestoreDeclineClicked : BackupSyncEvent
    data object OnDisableSyncConfirmClicked : BackupSyncEvent
    data object OnDisableSyncDeclineClicked : BackupSyncEvent
}
