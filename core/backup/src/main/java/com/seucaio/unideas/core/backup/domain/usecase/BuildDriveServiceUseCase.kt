package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository

class BuildDriveServiceUseCase(private val repository: GoogleAuthRepository) {
    operator fun invoke(account: GoogleSignInAccount): Drive = repository.buildDriveService(account)
}
