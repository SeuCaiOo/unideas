package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository

class GetSignInIntentUseCase(private val repository: GoogleAuthRepository) {
    operator fun invoke(): Intent = repository.getSignInIntent()
}
