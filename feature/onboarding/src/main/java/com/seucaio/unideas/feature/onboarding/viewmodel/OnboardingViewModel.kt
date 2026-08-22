package com.seucaio.unideas.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
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
    private val backupUseCase: BackupUseCase,
) : ViewModel() {

    val uiState: StateFlow<OnboardingUiState> =
        MutableStateFlow(OnboardingUiState.Ready).asStateFlow()

    private val _uiAction = Channel<OnboardingUiAction>(Channel.BUFFERED)
    val uiAction: Flow<OnboardingUiAction> = _uiAction.receiveAsFlow()

    private var signedInAccount: GoogleSignInAccount? = null

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnConnectClicked ->
                sendUiAction(OnboardingUiAction.LaunchGoogleSignIn(getSignInIntent()))

            OnboardingEvent.OnSkipClicked -> completeOnboarding()
            is OnboardingEvent.OnGoogleSignInResult -> handleSignInResult(event.account)
            is OnboardingEvent.OnRestoreBackupConfirmed -> restoreBackup(event.fileId)
            OnboardingEvent.OnStartFreshClicked -> completeOnboarding()
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            sendUiAction(OnboardingUiAction.ShowSnackbar(R.string.onboarding_signin_failed))
            return
        }
        signedInAccount = account
        checkExistingBackup(account)
    }

    private fun checkExistingBackup(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val backupInfo = backupUseCase.getLastBackupInfo(account).getOrNull()
            if (backupInfo != null) {
                _uiAction.send(OnboardingUiAction.ShowRestoreBackupSheet(account, backupInfo))
            } else {
                completeOnboardingSuspend()
            }
        }
    }

    private fun restoreBackup(fileId: String) {
        val account = signedInAccount ?: return
        viewModelScope.launch {
            val result = backupUseCase.restore(account, fileId)
            if (result.isSuccess) {
                finishOnboarding()
                _uiAction.send(OnboardingUiAction.RestoreCompleted)
            } else {
                sendUiAction(OnboardingUiAction.ShowSnackbar(R.string.onboarding_restore_failed))
            }
        }
    }

    private fun completeOnboarding() = viewModelScope.launch { completeOnboardingSuspend() }

    private suspend fun completeOnboardingSuspend() {
        finishOnboarding()
        _uiAction.send(OnboardingUiAction.OnboardingComplete)
    }

    private suspend fun finishOnboarding() = setOnboardingSeen(true)

    private fun sendUiAction(action: OnboardingUiAction) = viewModelScope.launch {
        _uiAction.send(action)
    }
}
