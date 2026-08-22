package com.seucaio.unideas.feature.onboarding.navigation

import kotlinx.serialization.Serializable

sealed interface OnboardingRoute {

    @Serializable
    data object Login : OnboardingRoute
}
