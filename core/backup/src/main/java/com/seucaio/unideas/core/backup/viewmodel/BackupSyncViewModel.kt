package com.seucaio.unideas.core.backup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
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
) : ViewModel() {

    private val _dialogState = MutableStateFlow<BackupSyncDialogState>(BackupSyncDialogState.None)
    val dialogState: StateFlow<BackupSyncDialogState> = _dialogState.asStateFlow()

    private val _uiAction = Channel<BackupSyncUiAction>(Channel.BUFFERED)
    val uiAction: Flow<BackupSyncUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: BackupSyncEvent) {
        when (event) {
            is BackupSyncEvent.OnDesyncDetected ->
                _dialogState.update { BackupSyncDialogState.RestorePrompt(event.remoteBackup) }

            is BackupSyncEvent.OnRestoreConfirmClicked -> handleRestore()

            is BackupSyncEvent.OnRestoreDeclineClicked -> {
                val remoteBackup = currentRemoteBackup() ?: return
                _dialogState.update { BackupSyncDialogState.DisableSyncConfirm(remoteBackup) }
            }

            is BackupSyncEvent.OnDisableSyncConfirmClicked -> handleDisableSync()

            is BackupSyncEvent.OnDisableSyncDeclineClicked -> {
                val remoteBackup = currentRemoteBackup() ?: return
                _dialogState.update { BackupSyncDialogState.RestorePrompt(remoteBackup) }
            }
        }
    }

    private fun currentRemoteBackup(): BackupInfo? =
        when (val state = _dialogState.value) {
            is BackupSyncDialogState.RestorePrompt -> state.remoteBackup
            is BackupSyncDialogState.DisableSyncConfirm -> state.remoteBackup
            BackupSyncDialogState.None -> null
        }

    private fun handleRestore() = viewModelScope.launch {
        val remoteBackup = currentRemoteBackup() ?: return@launch
        val account = getSignedInAccountUseCase() ?: return@launch
        backupUseCase.restore(account, remoteBackup.fileId)
            .onSuccess {
                autoBackupSettingsUseCase.setTrackedFileId(remoteBackup.fileId)
                _dialogState.update { BackupSyncDialogState.None }
                sendUiAction(BackupSyncUiAction.RestoreCompleted)
            }
            .onFailure { sendUiAction(BackupSyncUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleDisableSync() = viewModelScope.launch {
        autoBackupSettingsUseCase.setEnabled(false)
        _dialogState.update { BackupSyncDialogState.None }
    }

    private suspend fun sendUiAction(action: BackupSyncUiAction) = _uiAction.send(action)
}
