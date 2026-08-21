package com.seucaio.unideas.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.usecase.GetSignInIntentUseCase
import com.seucaio.unideas.domain.usecase.onboarding.SetOnboardingSeenUseCase
import com.seucaio.unideas.feature.onboarding.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val getSignInIntent: GetSignInIntentUseCase,
    private val setOnboardingSeen: SetOnboardingSeenUseCase,
) : ViewModel() {

    val uiState: StateFlow<OnboardingUiState> =
        MutableStateFlow(OnboardingUiState.Ready).asStateFlow()

    private val _uiAction = Channel<OnboardingUiAction>(Channel.BUFFERED)
    val uiAction: Flow<OnboardingUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnConnectClicked ->
                sendUiAction(OnboardingUiAction.LaunchGoogleSignIn(getSignInIntent()))

            OnboardingEvent.OnSkipClicked -> completeOnboarding()
            is OnboardingEvent.OnGoogleSignInResult -> handleSignInResult(event.account)
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            completeOnboarding()
        } else {
            sendUiAction(OnboardingUiAction.ShowSnackbar(R.string.onboarding_signin_failed))
        }
    }

    private fun completeOnboarding() = viewModelScope.launch {
        setOnboardingSeen(true)
        _uiAction.send(OnboardingUiAction.OnboardingComplete)
    }

    private fun sendUiAction(action: OnboardingUiAction) = viewModelScope.launch {
        _uiAction.send(action)
    }
}
