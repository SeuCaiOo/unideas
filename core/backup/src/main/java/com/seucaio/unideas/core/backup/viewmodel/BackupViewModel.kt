package com.seucaio.unideas.core.backup.viewmodel

import androidx.annotation.StringRes
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
    private val getBackupSyncStateUseCase: GetBackupSyncStateUseCase,
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
            _internalState.update { it.autoBackupEnabled(enabled) }
        }
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.OnConnectClick -> launchSignIn(BackupAction.Connect)
            BackupEvent.OnBackupClick -> launchSignIn(BackupAction.Upload)
            BackupEvent.OnToggleBackupListClick -> handleToggleBackupListClick()
            BackupEvent.OnRetryBackupListClick -> launchSignIn(BackupAction.Sync)
            is BackupEvent.OnGoogleSignInResult -> handleSignInResult(event.account, event.pendingAction)
            is BackupEvent.OnBackupSelected -> _internalState.update { it.selectBackup(event.fileId) }
            BackupEvent.OnRestoreClick -> handleRestoreClick()
            is BackupEvent.OnDeleteBackupClick ->
                viewModelScope.launch { _action.send(BackupUiAction.ShowDeleteConfirm(event.fileId)) }
            is BackupEvent.OnDeleteConfirmed -> delete(event.fileId)
            is BackupEvent.OnAutoBackupToggled -> handleAutoBackupToggled(event.enabled)
            is BackupEvent.OnOverwriteConfirmClicked -> handleOverwriteConfirmed(event.pending)
        }
    }

    private fun handleToggleBackupListClick() {
        if (_internalState.value.isBackupListVisible) {
            _internalState.update { it.hideBackupList() }
        } else {
            launchSignIn(BackupAction.Sync)
        }
    }

    private fun handleRestoreClick() {
        val fileId = _internalState.value.selectedBackupFileId
        val account = googleAuthUseCase.getSignedInAccount()
        if (fileId == null || account == null) return
        viewModelScope.launch { performRestore(account, fileId) }
    }

    private suspend fun performRestore(account: GoogleSignInAccount, fileId: String) {
        _internalState.update { it.startLoading() }
        backupUseCase.restore(account, fileId)
            .onSuccess {
                Timber.i("Backup: Restore use case finished successfully")
                autoBackupSettingsUseCase.setTrackedFileId(fileId)
                _internalState.update { it.connected() }
                _action.send(BackupUiAction.RestoreCompleted)
            }
            .onFailure {
                Timber.e(it, "Backup: Restore failed")
                _internalState.update { it.stopLoading() }
                showSnackbar(R.string.backup_error)
            }
    }

    private fun handleAutoBackupToggled(enabled: Boolean) = viewModelScope.launch {
        val account = googleAuthUseCase.getSignedInAccount()
        if (enabled && account != null &&
            getBackupSyncStateUseCase(account).getOrNull() is BackupSyncState.Desynced
        ) {
            _action.send(BackupUiAction.ShowOverwriteConfirm(PendingBackupOverwrite.EnableAutoBackup(account)))
            return@launch
        }
        autoBackupSettingsUseCase.setEnabled(enabled)
        _internalState.update { it.autoBackupEnabled(enabled) }
    }

    private fun handleOverwriteConfirmed(pending: PendingBackupOverwrite) {
        when (pending) {
            is PendingBackupOverwrite.Upload -> performUpload(pending.account)
            is PendingBackupOverwrite.EnableAutoBackup -> viewModelScope.launch {
                autoBackupSettingsUseCase.setEnabled(true)
                _internalState.update { it.autoBackupEnabled(true) }
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
            _internalState.update { it.startLoading() }
            backupUseCase.getLastBackupInfo(account)
                .onSuccess { info ->
                    _internalState.update { it.connected(lastBackupAt = info?.createdAt) }
                }
                .onFailure {
                    Timber.e(it, "Backup: Failed to get last backup info")
                    _internalState.update { it.stopLoading() }
                    if (!isInitialCheck) showSnackbar(R.string.backup_error)
                }
        }
    }

    private fun upload(account: GoogleSignInAccount) = viewModelScope.launch {
        val desynced = getBackupSyncStateUseCase(account).getOrNull() is BackupSyncState.Desynced
        if (desynced) {
            _action.send(BackupUiAction.ShowOverwriteConfirm(PendingBackupOverwrite.Upload(account)))
            return@launch
        }
        performUpload(account)
    }

    private fun performUpload(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _internalState.update { it.startLoading() }
            backupUseCase.upload(account)
                .onSuccess { info ->
                    autoBackupSettingsUseCase.setTrackedFileId(info.fileId)
                    _internalState.update { it.connected(lastBackupAt = info.createdAt) }
                    showSnackbar(R.string.backup_upload_success)
                }
                .onFailure {
                    Timber.e(it, "Backup: Upload failed")
                    _internalState.update { it.stopLoading() }
                    showSnackbar(R.string.backup_error)
                }
        }
    }

    private fun listBackups(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _internalState.update { it.startLoading() }
            backupUseCase.list(account)
                .onSuccess { backups ->
                    val autoFileId = autoBackupSettingsUseCase.getTrackedFileId()
                    val entries = backups.map { BackupListEntry(it, isAutomatic = it.fileId == autoFileId) }
                    val status = if (entries.isEmpty()) BackupListStatus.Empty else BackupListStatus.Loaded(entries)
                    _internalState.update { it.connected().showBackupList(status) }
                }
                .onFailure {
                    Timber.e(it, "Backup: List failed")
                    _internalState.update { it.stopLoading().showBackupList(BackupListStatus.Error) }
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

        fun startLoading(): InternalState = copy(isLoading = true)

        fun stopLoading(): InternalState = copy(isLoading = false)

        fun connected(lastBackupAt: LocalDateTime? = this.lastBackupAt): InternalState =
            copy(isLoading = false, isConnected = true, lastBackupAt = lastBackupAt)

        fun autoBackupEnabled(enabled: Boolean): InternalState = copy(isAutoBackupEnabled = enabled)

        fun removeBackup(fileId: String): InternalState {
            val status = backupListStatus
            if (status !is BackupListStatus.Loaded) return this
            val remaining = status.backups.filterNot { it.info.fileId == fileId }
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
