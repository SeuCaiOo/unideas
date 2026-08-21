package com.seucaio.unideas.domain.usecase.onboarding

import com.seucaio.unideas.domain.repository.OnboardingRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

class GetOnboardingSeenUseCase(private val repository: OnboardingRepository) : UseCase {

    suspend operator fun invoke(): Result<Boolean> = resultCatching {
        repository.isOnboardingSeen()
    }
}
