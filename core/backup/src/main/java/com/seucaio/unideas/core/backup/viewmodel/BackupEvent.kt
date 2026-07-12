package com.seucaio.unideas.core.backup.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface BackupEvent {
    data object OnBackupClick : BackupEvent
    data object OnSyncClick : BackupEvent
    data class OnGoogleSignInResult(
        val account: GoogleSignInAccount?,
        val pendingAction: BackupAction,
    ) : BackupEvent
    data class OnRestoreConfirmed(val account: GoogleSignInAccount, val fileId: String) : BackupEvent
}
