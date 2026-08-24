package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import androidx.annotation.StringRes

sealed interface BackupUiAction {
    data class ShowSnackbar(@StringRes val message: Int) : BackupUiAction
    data class LaunchGoogleSignIn(val intent: Intent, val pendingAction: BackupAction) : BackupUiAction
    data class ShowDeleteConfirm(val fileId: String) : BackupUiAction

    /**
     * Restore replaced the physical Room database file on disk — every already-created Room/
     * Koin singleton in this process still points at the old file handle. Rather than chase down
     * every place that could hold a stale reference, the sheet reacts to this by restarting the
     * whole app process, guaranteeing everything is rebuilt against the restored data.
     */
    data object RestoreCompleted : BackupUiAction
}
