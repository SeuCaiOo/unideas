package com.seucaio.unideas.feature.onboarding.viewmodel

import android.content.Intent
import androidx.annotation.StringRes

sealed interface OnboardingUiAction {

    data class LaunchGoogleSignIn(val intent: Intent) : OnboardingUiAction

    data object OnboardingComplete : OnboardingUiAction

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : OnboardingUiAction
}
