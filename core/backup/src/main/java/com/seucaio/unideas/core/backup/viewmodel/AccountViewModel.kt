package com.seucaio.unideas.core.backup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val googleAuthUseCase: GoogleAuthUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountUiState().withAccount(googleAuthUseCase.getSignedInAccount()),
    )
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<AccountUiAction>(Channel.BUFFERED)
    val uiAction: Flow<AccountUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: AccountEvent) {
        when (event) {
            AccountEvent.OnSignInClick -> onSignInClick()
            is AccountEvent.OnGoogleSignInResult -> onGoogleSignInResult(event.account)
            AccountEvent.OnSignOutClick -> onSignOutClick()
        }
    }

    private fun onSignInClick() = viewModelScope.launch {
        sendUiAction(AccountUiAction.LaunchGoogleSignIn(googleAuthUseCase.getSignInIntent()))
    }

    private fun onGoogleSignInResult(account: GoogleSignInAccount?) = viewModelScope.launch {
        if (account == null) {
            sendUiAction(AccountUiAction.ShowSnackbar(R.string.backup_sign_in_failed))
            return@launch
        }
        _uiState.update { it.withAccount(account) }
        sendUiAction(AccountUiAction.SignInCompleted)
    }

    private fun onSignOutClick() = viewModelScope.launch {
        googleAuthUseCase.signOut()
        _uiState.update { it.withAccount(null) }
        sendUiAction(AccountUiAction.SignedOut)
    }

    private suspend fun sendUiAction(action: AccountUiAction) = _uiAction.send(action)
}
