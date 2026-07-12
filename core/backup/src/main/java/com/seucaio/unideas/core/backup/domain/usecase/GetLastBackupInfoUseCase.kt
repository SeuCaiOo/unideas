package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

/** Most recent backup, if any — [ListBackupsUseCase] already orders results by newest first. */
class GetLastBackupInfoUseCase(private val listBackupsUseCase: ListBackupsUseCase) {
    suspend operator fun invoke(driveService: Drive): Result<BackupInfo?> =
        listBackupsUseCase(driveService).map { it.firstOrNull() }
}
