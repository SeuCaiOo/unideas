package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Facade over Google sign-in/session concerns — launching the sign-in flow and resolving the
 * current account. Building the [com.google.api.services.drive.Drive] service from an account
 * and everything that operates on it live in [BackupUseCase] instead, so callers deal only in
 * [GoogleSignInAccount] and never see the intermediate Drive service.
 */
class GoogleAuthUseCase(
    private val getSignInIntentUseCase: GetSignInIntentUseCase,
    private val getSignedInAccountUseCase: GetSignedInAccountUseCase,
) {

    fun getSignInIntent(): Intent = getSignInIntentUseCase()

    fun getSignedInAccount(): GoogleSignInAccount? = getSignedInAccountUseCase()
}
