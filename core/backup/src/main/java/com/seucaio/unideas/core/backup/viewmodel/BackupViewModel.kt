package com.seucaio.unideas.core.backup.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
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
import timber.log.Timber
import java.time.LocalDateTime

class BackupViewModel(
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val backupUseCase: BackupUseCase,
    private val autoBackupSettingsUseCase: AutoBackupSettingsUseCase,
) : ViewModel() {

    private val _internalState = MutableStateFlow(InternalState())

    val uiState: StateFlow<BackupUiState> = _internalState
        .map { state ->
            if (state.isLoading) {
                BackupUiState.Loading
            } else {
                BackupUiState.Ready(
                    isConnected = state.isConnected,
                    lastBackupAt = state.lastBackupAt,
                    isBackupListVisible = state.isBackupListVisible,
                    backupListStatus = state.backupListStatus,
                    selectedBackupFileId = state.selectedBackupFileId,
                    isAutoBackupEnabled = state.isAutoBackupEnabled,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = BackupUiState.Ready(),
        )

    private val _action = Channel<BackupUiAction>(Channel.CONFLATED)
    val action = _action.receiveAsFlow()

    init {
        val account = googleAuthUseCase.getSignedInAccount()
        if (account != null) refreshConnectionState(account, isInitialCheck = true)
        viewModelScope.launch {
            val enabled = autoBackupSettingsUseCase.isEnabled()
            _internalState.update { it.copy(isAutoBackupEnabled = enabled) }
        }
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.OnConnectClick -> launchSignIn(BackupAction.Connect)
            BackupEvent.OnBackupClick -> launchSignIn(BackupAction.Upload)
            BackupEvent.OnToggleBackupListClick -> if (_internalState.value.isBackupListVisible) {
                _internalState.update { it.hideBackupList() }
            } else {
                launchSignIn(BackupAction.Sync)
            }
            BackupEvent.OnRetryBackupListClick -> launchSignIn(BackupAction.Sync)
            is BackupEvent.OnGoogleSignInResult -> handleSignInResult(event.account, event.pendingAction)
            is BackupEvent.OnBackupSelected -> _internalState.update { it.selectBackup(event.fileId) }
            BackupEvent.OnRestoreClick -> {
                val fileId = _internalState.value.selectedBackupFileId
                val account = googleAuthUseCase.getSignedInAccount()
                if (fileId != null && account != null) restore(account, fileId)
            }
            is BackupEvent.OnDeleteBackupClick ->
                viewModelScope.launch { _action.send(BackupUiAction.ShowDeleteConfirm(event.fileId)) }
            is BackupEvent.OnDeleteConfirmed -> delete(event.fileId)
            is BackupEvent.OnAutoBackupToggled -> viewModelScope.launch {
                autoBackupSettingsUseCase.setEnabled(event.enabled)
                _internalState.update { it.copy(isAutoBackupEnabled = event.enabled) }
            }
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
            Timber.w("Backup: Google Sign-In result is null (user cancelled or error)")
            viewModelScope.launch { showSnackbar(R.string.backup_sign_in_failed) }
            return
        }
        when (pendingAction) {
            BackupAction.Connect -> refreshConnectionState(account, isInitialCheck = false)
            BackupAction.Upload -> upload(account)
            BackupAction.Sync -> listBackups(account)
        }
    }

    private fun refreshConnectionState(account: GoogleSignInAccount, isInitialCheck: Boolean) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.getLastBackupInfo(account)
                .onSuccess { info ->
                    _internalState.update {
                        it.copy(isLoading = false, isConnected = true, lastBackupAt = info?.createdAt)
                    }
                }
                .onFailure {
                    Timber.e(it, "Backup: Failed to get last backup info")
                    _internalState.update { it.copy(isLoading = false) }
                    if (!isInitialCheck) handleFailure()
                }
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
                .onFailure {
                    Timber.e(it, "Backup: Upload failed")
                    handleFailure()
                }
        }
    }

    private fun listBackups(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.list(account)
                .onSuccess { backups ->
                    val status = if (backups.isEmpty()) BackupListStatus.Empty else BackupListStatus.Loaded(backups)
                    _internalState.update { it.copy(isLoading = false, isConnected = true).showBackupList(status) }
                }
                .onFailure {
                    Timber.e(it, "Backup: List failed")
                    _internalState.update { it.copy(isLoading = false).showBackupList(BackupListStatus.Error) }
                }
        }
    }

    private fun restore(account: GoogleSignInAccount, fileId: String) {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true) }
            backupUseCase.restore(account, fileId)
                .onSuccess {
                    Timber.i("Backup: Restore use case finished successfully")
                    _internalState.update { it.copy(isLoading = false, isConnected = true) }
                    _action.send(BackupUiAction.RestoreCompleted)
                }
                .onFailure {
                    Timber.e(it, "Backup: Restore failed")
                    handleFailure()
                }
        }
    }

    private fun delete(fileId: String) {
        val account = googleAuthUseCase.getSignedInAccount() ?: return
        viewModelScope.launch {
            backupUseCase.delete(account, fileId)
                .onSuccess {
                    _internalState.update { it.removeBackup(fileId) }
                    showSnackbar(R.string.backup_delete_success)
                }
                .onFailure {
                    Timber.e(it, "Backup: Delete failed")
                    showSnackbar(R.string.backup_delete_error)
                }
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
        val isBackupListVisible: Boolean = false,
        val backupListStatus: BackupListStatus = BackupListStatus.Empty,
        val selectedBackupFileId: String? = null,
        val isAutoBackupEnabled: Boolean = false,
    ) {
        fun showBackupList(status: BackupListStatus): InternalState =
            copy(isBackupListVisible = true, backupListStatus = status)

        fun hideBackupList(): InternalState =
            copy(isBackupListVisible = false, selectedBackupFileId = null)

        fun selectBackup(fileId: String): InternalState = copy(selectedBackupFileId = fileId)

        fun removeBackup(fileId: String): InternalState {
            val status = backupListStatus
            if (status !is BackupListStatus.Loaded) return this
            val remaining = status.backups.filterNot { it.fileId == fileId }
            val newStatus = if (remaining.isEmpty()) BackupListStatus.Empty else BackupListStatus.Loaded(remaining)
            return copy(
                backupListStatus = newStatus,
                selectedBackupFileId = selectedBackupFileId.takeUnless { it == fileId },
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
