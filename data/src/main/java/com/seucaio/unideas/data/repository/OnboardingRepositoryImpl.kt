package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.datastore.OnboardingPreferences
import com.seucaio.unideas.domain.repository.OnboardingRepository

class OnboardingRepositoryImpl(
    private val preferences: OnboardingPreferences,
) : OnboardingRepository {

    override suspend fun isOnboardingSeen(): Boolean = preferences.isSeen()

    override suspend fun setOnboardingSeen(seen: Boolean) = preferences.setSeen(seen)
}
