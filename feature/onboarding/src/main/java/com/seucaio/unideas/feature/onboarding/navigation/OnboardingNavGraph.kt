package com.seucaio.unideas.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.feature.onboarding.OnboardingScreen

fun NavGraphBuilder.onboardingNavGraph(onOnboardingComplete: () -> Unit) {
    composable<OnboardingRoute.Login> {
        OnboardingScreen(onOnboardingComplete = onOnboardingComplete)
    }
}
