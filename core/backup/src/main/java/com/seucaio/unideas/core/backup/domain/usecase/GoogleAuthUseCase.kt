package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive

/**
 * Facade over the sign-in/session use cases — everything that doesn't take a [Drive] service as
 * input (it either produces one, like [buildDriveService], or resolves account state). The
 * backup-data operations that operate *on* a [Drive] service live in [BackupUseCase] instead.
 */
class GoogleAuthUseCase(
    private val getSignInIntentUseCase: GetSignInIntentUseCase,
    private val getSignedInAccountUseCase: GetSignedInAccountUseCase,
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase,
) {

    fun getSignInIntent(): Intent = getSignInIntentUseCase()

    fun getSignedInAccount(): GoogleSignInAccount? = getSignedInAccountUseCase()

    fun buildDriveService(account: GoogleSignInAccount): Drive = buildDriveServiceUseCase(account)
}
