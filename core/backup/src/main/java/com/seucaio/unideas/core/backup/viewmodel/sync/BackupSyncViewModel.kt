package com.seucaio.unideas.core.backup.viewmodel.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetConfirmedBackupSyncStateUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignedInAccountUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackupSyncViewModel(
    private val getSignedInAccountUseCase: GetSignedInAccountUseCase,
    private val backupUseCase: BackupUseCase,
    private val autoBackupSettingsUseCase: AutoBackupSettingsUseCase,
    private val getConfirmedBackupSyncStateUseCase: GetConfirmedBackupSyncStateUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow<BackupSyncDialogState>(BackupSyncDialogState.None)
    val dialogState: StateFlow<BackupSyncDialogState> = _dialogState.asStateFlow()

    private val _syncCheckCompleted = MutableStateFlow(false)
    val syncCheckCompleted: StateFlow<Boolean> = _syncCheckCompleted.asStateFlow()

    private val _uiAction = Channel<BackupSyncUiAction>(Channel.BUFFERED)
    val uiAction: Flow<BackupSyncUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: BackupSyncEvent) {
        when (event) {
            is BackupSyncEvent.OnSyncCheckRequested -> handleSyncCheckRequested()
            is BackupSyncEvent.OnRestoreConfirmClicked -> handleRestore()
            is BackupSyncEvent.OnRestoreDeclineClicked -> handleRestoreDecline()
            is BackupSyncEvent.OnDisableSyncConfirmClicked -> handleDisableSync()
            is BackupSyncEvent.OnDisableSyncDeclineClicked -> handleDisableSyncDecline()
        }
    }

    private fun handleSyncCheckRequested() = viewModelScope.launch {
        _syncCheckCompleted.update { false }
        if (autoBackupSettingsUseCase.isEnabled()) {
            val account = getSignedInAccountUseCase()
            if (account != null) {
                val confirmedDesync = getConfirmedBackupSyncStateUseCase(account)
                if (confirmedDesync != null) {
                    _dialogState.update { BackupSyncDialogState.RestorePrompt(confirmedDesync) }
                }
            }
        }
        _syncCheckCompleted.update { true }
    }

    private fun currentRemoteBackup(): BackupInfo? =
        when (val state = _dialogState.value) {
            is BackupSyncDialogState.RestorePrompt -> state.remoteBackup
            is BackupSyncDialogState.DisableSyncConfirm -> state.remoteBackup
            BackupSyncDialogState.None -> null
        }

    private fun handleRestoreDecline() {
        val remoteBackup = currentRemoteBackup() ?: return
        _dialogState.update { BackupSyncDialogState.DisableSyncConfirm(remoteBackup) }
    }

    private fun handleRestore() {
        val prompt = _dialogState.value as? BackupSyncDialogState.RestorePrompt ?: return
        if (prompt.isRestoring) return
        _dialogState.update { prompt.restoring(true) }
        viewModelScope.launch { performRestore(prompt) }
    }

    private suspend fun performRestore(prompt: BackupSyncDialogState.RestorePrompt) {
        val account = getSignedInAccountUseCase()
        if (account == null) {
            _dialogState.update { prompt.restoring(false) }
            return
        }
        backupUseCase.restore(account, prompt.remoteBackup.fileId)
            .onSuccess {
                autoBackupSettingsUseCase.setTrackedFileId(prompt.remoteBackup.fileId)
                _dialogState.update { BackupSyncDialogState.None }
                sendUiAction(BackupSyncUiAction.RestoreCompleted)
            }
            .onFailure {
                _dialogState.update { prompt.restoring(false) }
                sendUiAction(BackupSyncUiAction.ShowError(it.message.orEmpty()))
            }
    }

    private fun handleDisableSync() = viewModelScope.launch {
        autoBackupSettingsUseCase.setEnabled(false)
        _dialogState.update { BackupSyncDialogState.None }
        sendUiAction(BackupSyncUiAction.ShowSnackbar(R.string.backup_auto_backup_disabled))
    }

    private fun handleDisableSyncDecline() {
        val remoteBackup = currentRemoteBackup() ?: return
        _dialogState.update { BackupSyncDialogState.RestorePrompt(remoteBackup) }
    }

    private suspend fun sendUiAction(action: BackupSyncUiAction) = _uiAction.send(action)
}
