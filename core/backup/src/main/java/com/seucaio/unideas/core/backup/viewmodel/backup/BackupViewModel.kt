package com.seucaio.unideas.core.backup.viewmodel.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetBackupSyncStateUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class BackupViewModel(
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val backupUseCase: BackupUseCase,
    private val autoBackupSettingsUseCase: AutoBackupSettingsUseCase,
    private val getBackupSyncStateUseCase: GetBackupSyncStateUseCase,
) : ViewModel() {

    private val _connectionState = MutableStateFlow(ConnectionState())
    private val _backupListState = MutableStateFlow(BackupListState())
    private val _isAutoBackupEnabled = MutableStateFlow(false)

    val uiState: StateFlow<BackupUiState> = combine(
        _connectionState,
        _backupListState,
        _isAutoBackupEnabled,
    ) { connection, list, autoBackupEnabled ->
        if (connection.isLoading) {
            BackupUiState.Loading
        } else {
            BackupUiState.Ready(
                isConnected = connection.isConnected,
                lastBackupAt = connection.lastBackupAt,
                isBackupListVisible = list.isVisible,
                backupListStatus = list.status,
                selectedBackupFileId = list.selectedFileId,
                isAutoBackupEnabled = autoBackupEnabled,
            )
        }
    }.stateIn(
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
            _isAutoBackupEnabled.update { autoBackupSettingsUseCase.isEnabled() }
        }
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.OnConnectClick -> launchSignIn(BackupAction.Connect)
            BackupEvent.OnBackupClick -> launchSignIn(BackupAction.Upload)
            BackupEvent.OnToggleBackupListClick -> handleToggleBackupListClick()
            BackupEvent.OnRetryBackupListClick -> launchSignIn(BackupAction.Sync)
            is BackupEvent.OnGoogleSignInResult -> handleSignInResult(
                event.account,
                event.pendingAction
            )

            is BackupEvent.OnBackupSelected -> _backupListState.update { it.select(event.fileId) }
            BackupEvent.OnRestoreClick -> handleRestoreClick()
            is BackupEvent.OnDeleteBackupClick ->
                viewModelScope.launch { sendUiAction(BackupUiAction.ShowDeleteConfirm(event.fileId)) }

            is BackupEvent.OnDeleteConfirmed -> delete(event.fileId)
            is BackupEvent.OnAutoBackupToggled -> handleAutoBackupToggled(event.enabled)
            is BackupEvent.OnOverwriteConfirmClicked -> handleOverwriteConfirmed(event.pending)
        }
    }

    //region Sign-in
    private fun launchSignIn(pendingAction: BackupAction) {
        viewModelScope.launch {
            val intent = googleAuthUseCase.getSignInIntent()
            sendUiAction(BackupUiAction.LaunchGoogleSignIn(intent, pendingAction))
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?, pendingAction: BackupAction) {
        if (account == null) {
            Timber.w("Backup: Google Sign-In result is null (user cancelled or error)")
            viewModelScope.launch { sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_sign_in_failed)) }
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
            _connectionState.update { it.startLoading() }
            backupUseCase.getLastBackupInfo(account)
                .onSuccess { info ->
                    _connectionState.update { it.connected(lastBackupAt = info?.createdAt) }
                }
                .onFailure {
                    Timber.e(it, "Backup: Failed to get last backup info")
                    _connectionState.update { it.stopLoading() }
                    if (!isInitialCheck) sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_error))
                }
        }
    }
    //endregion

    //region Upload
    private fun upload(account: GoogleSignInAccount) = viewModelScope.launch {
        if (isDesynced(account)) {
            sendUiAction(BackupUiAction.ShowOverwriteConfirm(PendingBackupOverwrite.Upload(account)))
            return@launch
        }
        performUpload(account)
    }

    private fun performUpload(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _connectionState.update { it.startLoading() }
            backupUseCase.upload(account)
                .onSuccess { info ->
                    autoBackupSettingsUseCase.setTrackedFileId(info.fileId)
                    _connectionState.update { it.connected(lastBackupAt = info.createdAt) }
                    sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_upload_success))
                }
                .onFailure {
                    Timber.e(it, "Backup: Upload failed")
                    _connectionState.update { it.stopLoading() }
                    sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_error))
                }
        }
    }
    //endregion

    //region Restore
    private fun handleRestoreClick() {
        val fileId = _backupListState.value.selectedFileId
        val account = googleAuthUseCase.getSignedInAccount()
        if (fileId == null || account == null) return
        viewModelScope.launch { performRestore(account, fileId) }
    }

    private suspend fun performRestore(account: GoogleSignInAccount, fileId: String) {
        _connectionState.update { it.startLoading() }
        backupUseCase.restore(account, fileId)
            .onSuccess {
                Timber.i("Backup: Restore use case finished successfully")
                autoBackupSettingsUseCase.setTrackedFileId(fileId)
                _connectionState.update { it.connected() }
                sendUiAction(BackupUiAction.RestoreCompleted)
            }
            .onFailure {
                Timber.e(it, "Backup: Restore failed")
                _connectionState.update { it.stopLoading() }
                sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_error))
            }
    }
    //endregion

    //region Backup list
    private fun handleToggleBackupListClick() {
        if (_backupListState.value.isVisible) {
            _backupListState.update { it.hide() }
        } else {
            launchSignIn(BackupAction.Sync)
        }
    }

    private fun listBackups(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _connectionState.update { it.startLoading() }
            backupUseCase.list(account)
                .onSuccess { backups ->
                    val autoFileId = autoBackupSettingsUseCase.getTrackedFileId()
                    val entries =
                        backups.map { BackupListEntry(it, isAutomatic = it.fileId == autoFileId) }
                    val status =
                        if (entries.isEmpty()) {
                            BackupListStatus.Empty
                        } else {
                            BackupListStatus.Loaded(entries)
                        }
                    _connectionState.update { it.connected() }
                    _backupListState.update { it.show(status) }
                }
                .onFailure {
                    Timber.e(it, "Backup: List failed")
                    _connectionState.update { it.stopLoading() }
                    _backupListState.update { it.show(BackupListStatus.Error) }
                }
        }
    }

    private fun delete(fileId: String) {
        val account = googleAuthUseCase.getSignedInAccount() ?: return
        viewModelScope.launch {
            backupUseCase.delete(account, fileId)
                .onSuccess {
                    _backupListState.update { it.removeBackup(fileId) }
                    sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_delete_success))
                }
                .onFailure {
                    Timber.e(it, "Backup: Delete failed")
                    sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_delete_error))
                }
        }
    }
    //endregion

    //region Auto-backup
    private fun handleAutoBackupToggled(enabled: Boolean) = viewModelScope.launch {
        val account = googleAuthUseCase.getSignedInAccount()
        if (enabled && account != null && isDesynced(account)) {
            sendUiAction(
                BackupUiAction.ShowOverwriteConfirm(
                    PendingBackupOverwrite.EnableAutoBackup(
                        account
                    )
                )
            )
            return@launch
        }
        autoBackupSettingsUseCase.setEnabled(enabled)
        _isAutoBackupEnabled.update { enabled }
        if (!enabled) sendUiAction(BackupUiAction.ShowSnackbar(R.string.backup_auto_backup_disabled))
    }

    private fun handleOverwriteConfirmed(pending: PendingBackupOverwrite) {
        when (pending) {
            is PendingBackupOverwrite.Upload -> performUpload(pending.account)
            is PendingBackupOverwrite.EnableAutoBackup -> viewModelScope.launch {
                autoBackupSettingsUseCase.setEnabled(true)
                _isAutoBackupEnabled.update { true }
            }
        }
    }

    private suspend fun isDesynced(account: GoogleSignInAccount): Boolean =
        getBackupSyncStateUseCase(account).getOrNull() is BackupSyncState.Desynced
    //endregion

    private suspend fun sendUiAction(action: BackupUiAction) = _action.send(action)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
