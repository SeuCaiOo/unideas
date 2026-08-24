package com.seucaio.unideas.domain.usecase.onboarding

import com.seucaio.unideas.domain.repository.OnboardingRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

class SetOnboardingSeenUseCase(private val repository: OnboardingRepository) : UseCase {

    suspend operator fun invoke(seen: Boolean): Result<Unit> = resultCatching {
        repository.setOnboardingSeen(seen)
    }
}
