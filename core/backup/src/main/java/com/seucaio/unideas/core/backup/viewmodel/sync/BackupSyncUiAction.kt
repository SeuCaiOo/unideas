package com.seucaio.unideas.core.backup.viewmodel.sync

import androidx.annotation.StringRes

sealed interface BackupSyncUiAction {
    data object RestoreCompleted : BackupSyncUiAction
    data class ShowError(val message: String) : BackupSyncUiAction
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : BackupSyncUiAction
}
