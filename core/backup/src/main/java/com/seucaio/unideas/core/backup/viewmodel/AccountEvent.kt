package com.seucaio.unideas.core.backup.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface AccountEvent {
    data object OnSignInClick : AccountEvent
    data class OnGoogleSignInResult(val account: GoogleSignInAccount?) : AccountEvent
    data object OnSignOutClick : AccountEvent
}
