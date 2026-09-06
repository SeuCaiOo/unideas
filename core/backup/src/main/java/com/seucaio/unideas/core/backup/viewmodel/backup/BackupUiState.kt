package com.seucaio.unideas.core.backup.viewmodel.backup

import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import java.time.LocalDateTime

sealed interface BackupUiState {
    data object Loading : BackupUiState
    data class Ready(
        val isConnected: Boolean = false,
        val lastBackupAt: LocalDateTime? = null,
        val isBackupListVisible: Boolean = false,
        val backupListStatus: BackupListStatus = BackupListStatus.Empty,
        val selectedBackupFileId: String? = null,
        val isAutoBackupEnabled: Boolean = false,
    ) : BackupUiState
}

sealed interface BackupListStatus {
    data object Empty : BackupListStatus
    data object Error : BackupListStatus
    data class Loaded(val backups: List<BackupListEntry>) : BackupListStatus
}

data class BackupListEntry(val info: BackupInfo, val isAutomatic: Boolean = false)

data class BackupListState(
    val isVisible: Boolean = false,
    val status: BackupListStatus = BackupListStatus.Empty,
    val selectedFileId: String? = null,
) {
    fun show(status: BackupListStatus): BackupListState =
        copy(isVisible = true, status = status)

    fun hide(): BackupListState = copy(isVisible = false, selectedFileId = null)

    fun select(fileId: String): BackupListState = copy(selectedFileId = fileId)

    fun removeBackup(fileId: String): BackupListState {
        val current = status
        if (current !is BackupListStatus.Loaded) return this
        val remaining = current.backups.filterNot { it.info.fileId == fileId }
        val newStatus = if (remaining.isEmpty()) {
            BackupListStatus.Empty
        } else {
            BackupListStatus.Loaded(remaining)
        }
        return copy(status = newStatus, selectedFileId = selectedFileId.takeUnless { it == fileId })
    }
}

data class ConnectionState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val lastBackupAt: LocalDateTime? = null,
) {
    fun startLoading(): ConnectionState = copy(isLoading = true)

    fun stopLoading(): ConnectionState = copy(isLoading = false)

    fun connected(lastBackupAt: LocalDateTime? = this.lastBackupAt): ConnectionState =
        copy(isLoading = false, isConnected = true, lastBackupAt = lastBackupAt)
}
