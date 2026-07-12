package com.seucaio.unideas.core.backup.domain.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive

/**
 * Scoped Google Sign-In (`DriveScopes.DRIVE_APPDATA`) and Drive client construction.
 * Not Firebase Auth — this app has no general login, only this Drive-specific flow.
 */
interface GoogleAuthRepository {

    /** Intent to launch the scoped Google Sign-In flow. */
    fun getSignInIntent(): Intent

    /** Already-signed-in account for this app's scope, if any — null when disconnected. */
    fun getSignedInAccount(): GoogleSignInAccount?

    /** Builds a [Drive] client authenticated as [account], scoped to the app data folder. */
    fun buildDriveService(account: GoogleSignInAccount): Drive
}
