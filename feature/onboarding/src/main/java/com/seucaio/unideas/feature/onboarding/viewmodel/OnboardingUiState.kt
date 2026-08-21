package com.seucaio.unideas.feature.onboarding.viewmodel

sealed interface OnboardingUiState {

    data object Ready : OnboardingUiState
}
