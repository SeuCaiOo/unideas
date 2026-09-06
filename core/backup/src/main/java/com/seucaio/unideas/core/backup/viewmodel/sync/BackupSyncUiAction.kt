package com.seucaio.unideas.core.backup.viewmodel.sync

sealed interface BackupSyncUiAction {
    data object RestoreCompleted : BackupSyncUiAction
    data class ShowError(val message: String) : BackupSyncUiAction
}
