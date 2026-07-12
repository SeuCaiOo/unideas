package com.seucaio.unideas.core.backup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.core.backup.viewmodel.BackupUiState
import java.time.LocalDateTime

class BackupPreviewProvider : PreviewParameterProvider<BackupUiState> {
    override val values: Sequence<BackupUiState> = sequenceOf(
        BackupUiState.Ready(),
        BackupUiState.Ready(lastBackupAt = LocalDateTime.of(2026, 5, 7, 8, 30)),
        BackupUiState.Loading,
    )
}
