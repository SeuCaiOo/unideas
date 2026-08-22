package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository

class SignOutUseCase(private val repository: GoogleAuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}
