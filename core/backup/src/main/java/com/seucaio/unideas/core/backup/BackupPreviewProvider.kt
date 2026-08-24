package com.seucaio.unideas.core.backup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.viewmodel.BackupListStatus
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import java.time.LocalDateTime

class BackupPreviewProvider : PreviewParameterProvider<BackupUiState> {
    private val previewBackups = listOf(
        BackupInfo(fileId = "1", createdAt = LocalDateTime.now().withHour(8).withMinute(30), sizeBytes = 204_800),
        BackupInfo(
            fileId = "2",
            createdAt = LocalDateTime.now().minusDays(1).withHour(20).withMinute(15),
            sizeBytes = 198_000,
        ),
        BackupInfo(fileId = "3", createdAt = LocalDateTime.of(2026, 5, 5, 11, 12), sizeBytes = 190_500),
    )

    private val manyPreviewBackups = previewBackups + (4..20).map { index ->
        BackupInfo(
            fileId = index.toString(),
            createdAt = LocalDateTime.of(2026, 4, index, 9, 0),
            sizeBytes = 180_000L,
        )
    }

    override val values: Sequence<BackupUiState> = sequenceOf(
        BackupUiState.Ready(isConnected = false),
        BackupUiState.Ready(isConnected = true),
        BackupUiState.Ready(isConnected = true, lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30)),
        BackupUiState.Ready(
            isConnected = true,
            lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30),
            isBackupListVisible = true,
            backupListStatus = BackupListStatus.Loaded(previewBackups),
        ),
        BackupUiState.Ready(
            isConnected = true,
            lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30),
            isBackupListVisible = true,
            backupListStatus = BackupListStatus.Loaded(previewBackups),
            selectedBackupFileId = "2",
        ),
        BackupUiState.Ready(
            isConnected = true,
            lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30),
            isBackupListVisible = true,
            backupListStatus = BackupListStatus.Loaded(manyPreviewBackups),
            selectedBackupFileId = "5",
        ),
        BackupUiState.Ready(
            isConnected = true,
            lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30),
            isBackupListVisible = true,
            backupListStatus = BackupListStatus.Empty,
        ),
        BackupUiState.Ready(
            isConnected = true,
            lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30),
            isBackupListVisible = true,
            backupListStatus = BackupListStatus.Error,
        ),
        BackupUiState.Loading,
    )
}
