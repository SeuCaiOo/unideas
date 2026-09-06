package com.seucaio.unideas.core.backup.domain.model

sealed interface BackupSyncState {
    data object Synced : BackupSyncState
    data class Desynced(val remoteBackup: BackupInfo) : BackupSyncState
    data object NoRemoteBackup : BackupSyncState
}
