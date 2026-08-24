package com.seucaio.unideas.feature.onboarding.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface OnboardingEvent {

    data object OnConnectClicked : OnboardingEvent

    data object OnSkipClicked : OnboardingEvent

    data class OnGoogleSignInResult(val account: GoogleSignInAccount?) : OnboardingEvent

    data class OnRestoreBackupConfirmed(val fileId: String) : OnboardingEvent

    data object OnStartFreshClicked : OnboardingEvent
}
