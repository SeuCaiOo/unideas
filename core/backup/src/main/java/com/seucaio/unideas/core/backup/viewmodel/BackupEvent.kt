package com.seucaio.unideas.core.backup.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface BackupEvent {
    data object OnConnectClick : BackupEvent
    data object OnBackupClick : BackupEvent
    data object OnToggleBackupListClick : BackupEvent
    data object OnRetryBackupListClick : BackupEvent
    data class OnGoogleSignInResult(
        val account: GoogleSignInAccount?,
        val pendingAction: BackupAction,
    ) : BackupEvent
    data class OnBackupSelected(val fileId: String) : BackupEvent
    data object OnRestoreClick : BackupEvent
    data class OnDeleteBackupClick(val fileId: String) : BackupEvent
    data class OnDeleteConfirmed(val fileId: String) : BackupEvent
    data class OnAutoBackupToggled(val enabled: Boolean) : BackupEvent
}
