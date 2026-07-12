package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import androidx.annotation.StringRes
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

sealed interface BackupUiAction {
    data class ShowSnackbar(@StringRes val message: Int) : BackupUiAction
    data class LaunchGoogleSignIn(val intent: Intent, val pendingAction: BackupAction) : BackupUiAction
    data class ShowRestoreDialog(val backups: List<BackupInfo>) : BackupUiAction
}
