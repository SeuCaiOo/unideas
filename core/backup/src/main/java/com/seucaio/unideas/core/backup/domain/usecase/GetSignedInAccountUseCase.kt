package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository

class GetSignedInAccountUseCase(private val repository: GoogleAuthRepository) {
    operator fun invoke(): GoogleSignInAccount? = repository.getSignedInAccount()
}
