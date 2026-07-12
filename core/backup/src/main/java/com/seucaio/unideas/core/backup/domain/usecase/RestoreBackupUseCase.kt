package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository

class RestoreBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(driveService: Drive, fileId: String): Result<Unit> =
        repository.restoreBackup(driveService, fileId)
}
