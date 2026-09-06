package com.seucaio.unideas.core.backup.viewmodel.backup

import android.content.Intent
import androidx.annotation.StringRes

sealed interface BackupUiAction {
    data class ShowSnackbar(@param:StringRes val message: Int) : BackupUiAction
    data class LaunchGoogleSignIn(val intent: Intent, val pendingAction: BackupAction) : BackupUiAction
    data class ShowDeleteConfirm(val fileId: String) : BackupUiAction
    data class ShowOverwriteConfirm(val pending: PendingBackupOverwrite) : BackupUiAction
    data object RestoreCompleted : BackupUiAction
}
