package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository

class UploadBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(driveService: Drive): Result<BackupInfo> =
        repository.uploadBackup(driveService)
}
