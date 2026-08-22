package com.seucaio.unideas.core.backup.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
import com.seucaio.unideas.domain.usecase.onboarding.SetOnboardingSeenUseCase
import com.seucaio.unideas.domain.usecase.settings.ClearDatabaseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountViewModel(
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val backupUseCase: BackupUseCase,
    private val clearDatabaseUseCase: ClearDatabaseUseCase,
    private val setOnboardingSeenUseCase: SetOnboardingSeenUseCase,
) : ViewModel() {

    private val _internalState = MutableStateFlow(InternalState())

    val uiState: StateFlow<AccountUiState> = _internalState
        .map { state ->
            AccountUiState.Ready(
                isConnected = state.currentAccount != null,
                accountName = state.currentAccount?.displayName,
                accountEmail = state.currentAccount?.email,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AccountUiState.Ready(),
        )

    private val _action = Channel<AccountUiAction>(Channel.CONFLATED)
    val action = _action.receiveAsFlow()

    init {
        val account = googleAuthUseCase.getSignedInAccount()
        if (account != null) _internalState.update { it.copy(currentAccount = account) }
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            AccountEvent.OnSwitchAccountClick -> onSwitchAccountClick()
            AccountEvent.OnSwitchAccountConfirmed -> onSwitchAccountConfirmed()
            is AccountEvent.OnSwitchAccountGoogleSignInResult ->
                handleSwitchAccountSignInResult(event.account)

            is AccountEvent.OnSwitchAccountRestoreConfirmed -> onSwitchAccountRestoreConfirmed(event.fileId)
            AccountEvent.OnSwitchAccountStartFreshConfirmed -> onSwitchAccountStartFreshConfirmed()

            AccountEvent.OnLogoutClick -> onLogoutClick()
            AccountEvent.OnLogoutConfirmed -> onLogoutConfirmed()
        }
    }

    private fun onSwitchAccountClick() = viewModelScope.launch {
        _action.send(AccountUiAction.ShowSwitchAccountDialog)
    }

    private fun onSwitchAccountConfirmed() {
        viewModelScope.launch {
            _internalState.value.currentAccount?.let { backupUseCase.upload(it) }
            val intent = googleAuthUseCase.getSignInIntent()
            _action.send(AccountUiAction.LaunchSwitchAccountSignIn(intent))
        }
    }

    private fun handleSwitchAccountSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            viewModelScope.launch { showSnackbar(R.string.backup_sign_in_failed) }
            return
        }
        viewModelScope.launch {
            _internalState.update { it.copy(pendingSwitchAccount = account) }
            val backupInfo = backupUseCase.getLastBackupInfo(account).getOrNull()
            if (backupInfo != null) {
                _action.send(AccountUiAction.ShowSwitchAccountRestoreSheet(backupInfo))
            } else {
                completeSwitchAccount(account, restarted = false)
            }
        }
    }

    private fun onSwitchAccountRestoreConfirmed(fileId: String) {
        val account = _internalState.value.pendingSwitchAccount ?: return
        viewModelScope.launch {
            backupUseCase.restore(account, fileId)
                .onSuccess { completeSwitchAccount(account, restarted = true) }
                .onFailure {
                    Timber.e(it, "Account: Switch account restore failed")
                    showSnackbar(R.string.backup_error)
                }
        }
    }

    private fun onSwitchAccountStartFreshConfirmed() {
        val account = _internalState.value.pendingSwitchAccount ?: return
        viewModelScope.launch { completeSwitchAccount(account, restarted = false) }
    }

    private suspend fun completeSwitchAccount(account: GoogleSignInAccount, restarted: Boolean) {
        if (!restarted) clearDatabaseUseCase()
        _internalState.update { it.copy(currentAccount = account, pendingSwitchAccount = null) }
        _action.send(AccountUiAction.SwitchAccountCompleted(restarted))
    }

    private fun onLogoutClick() = viewModelScope.launch {
        _action.send(AccountUiAction.ShowLogoutDialog)
    }

    private fun onLogoutConfirmed() {
        viewModelScope.launch {
            _internalState.value.currentAccount?.let { backupUseCase.upload(it) }
            googleAuthUseCase.signOut()
            setOnboardingSeenUseCase(false)
            _action.send(AccountUiAction.LogoutCompleted)
        }
    }

    private suspend fun showSnackbar(@StringRes message: Int) =
        _action.send(AccountUiAction.ShowSnackbar(message))

    private data class InternalState(
        val currentAccount: GoogleSignInAccount? = null,
        val pendingSwitchAccount: GoogleSignInAccount? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
