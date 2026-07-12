package com.seucaio.unideas.core.backup.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BackupViewModel(
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val backupUseCase: BackupUseCase,
) : ViewModel() {

    private val _internalState = MutableStateFlow(InternalState())

    val uiState: StateFlow<BackupUiState> = _internalState
        .map { state ->
            if (state.isLoading) {
                BackupUiState.Loading
            } else {
                BackupUiState.Ready(isConnected = state.isConnected, lastBackupAt = state.lastBackupAt)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = BackupUiState.Ready(),
        )

    private val _action = Channel<BackupUiAction>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    init {
        val account = googleAuthUseCase.getSignedInAccount()
        if (account != null) refreshConnectionState(account)
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.OnConnectClick -> launchSignIn(BackupAction.Connect)
            BackupEvent.OnBackupClick -> launchSignIn(BackupAction.Upload)
            BackupEvent.OnSyncClick -> launchSignIn(BackupAction.Sync)
            is BackupEvent.OnGoogleSignInResult -> handleSignInResult(event.account, event.pendingAction)
            is BackupEvent.OnRestoreConfirmed -> restore(event.account, event.fileId)
        }
    }

    private fun launchSignIn(pendingAction: BackupAction) {
        viewModelScope.launch {
            val intent = googleAuthUseCase.getSignInIntent()
            _action.send(BackupUiAction.LaunchGoogleSignIn(intent, pendingAction))
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?, pendingAction: BackupAction) {
        if (account == null) {
            viewModelScope.launch { showSnackbar(R.string.backup_sign_in_failed) }
            return
        }
        when (pendingAction) {
            BackupAction.Connect -> refreshConnectionState(account)
            BackupAction.Upload -> upload(account)
            BackupAction.Sync -> listBackups(account)
        }
    }

    private fun refreshConnectionState(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.getLastBackupInfo(account)
                .onSuccess { info ->
                    _internalState.update {
                        it.copy(isLoading = false, isConnected = true, lastBackupAt = info?.createdAt)
                    }
                }
                .onFailure { handleFailure() }
        }
    }

    private fun upload(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.upload(account)
                .onSuccess { info ->
                    _internalState.update {
                        it.copy(isLoading = false, isConnected = true, lastBackupAt = info.createdAt)
                    }
                    showSnackbar(R.string.backup_upload_success)
                }
                .onFailure { handleFailure() }
        }
    }

    private fun listBackups(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.list(account)
                .onSuccess { backups ->
                    _internalState.update { it.copy(isLoading = false, isConnected = true) }
                    if (backups.isEmpty()) {
                        showSnackbar(R.string.backup_no_backups_found)
                    } else {
                        _action.send(BackupUiAction.ShowRestoreDialog(backups))
                    }
                }
                .onFailure { handleFailure() }
        }
    }

    private fun restore(account: GoogleSignInAccount, fileId: String) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.restore(account, fileId)
                .onSuccess {
                    _internalState.update { it.copy(isLoading = false, isConnected = true) }
                    showSnackbar(R.string.backup_restore_success)
                }
                .onFailure { handleFailure() }
        }
    }

    private suspend fun handleFailure() {
        _internalState.update { it.copy(isLoading = false) }
        showSnackbar(R.string.backup_error)
    }

    private suspend fun showSnackbar(@StringRes message: Int) =
        _action.send(BackupUiAction.ShowSnackbar(message))

    private data class InternalState(
        val isLoading: Boolean = false,
        val isConnected: Boolean = false,
        val lastBackupAt: LocalDateTime? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
