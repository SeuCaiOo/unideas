package com.seucaio.unideas.core.backup.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface PendingBackupOverwrite {
    data class Upload(val account: GoogleSignInAccount) : PendingBackupOverwrite
    data class EnableAutoBackup(val account: GoogleSignInAccount) : PendingBackupOverwrite
}
