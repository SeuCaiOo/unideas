package com.seucaio.unideas.domain.repository

interface OnboardingRepository {

    suspend fun isOnboardingSeen(): Boolean

    suspend fun setOnboardingSeen(seen: Boolean)
}
