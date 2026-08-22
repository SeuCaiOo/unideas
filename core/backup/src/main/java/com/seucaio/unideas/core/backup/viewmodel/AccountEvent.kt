package com.seucaio.unideas.core.backup.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface AccountEvent {
    data object OnSwitchAccountClick : AccountEvent
    data object OnSwitchAccountConfirmed : AccountEvent
    data class OnSwitchAccountGoogleSignInResult(val account: GoogleSignInAccount?) : AccountEvent
    data class OnSwitchAccountRestoreConfirmed(val fileId: String) : AccountEvent
    data object OnSwitchAccountStartFreshConfirmed : AccountEvent

    data object OnLogoutClick : AccountEvent
    data object OnLogoutConfirmed : AccountEvent
}
