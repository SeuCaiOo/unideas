package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

/**
 * Facade over the backup-data use cases — everything that operates *on* an already-built
 * [Drive] service. Sign-in/session concerns (getting the intent, resolving the current account,
 * building the [Drive] service itself) live in [GoogleAuthUseCase] instead.
 */
class BackupUseCase(
    private val uploadBackupUseCase: UploadBackupUseCase,
    private val listBackupsUseCase: ListBackupsUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val getLastBackupInfoUseCase: GetLastBackupInfoUseCase,
) {

    suspend fun upload(driveService: Drive): Result<BackupInfo> = uploadBackupUseCase(driveService)

    suspend fun list(driveService: Drive): Result<List<BackupInfo>> = listBackupsUseCase(driveService)

    suspend fun restore(driveService: Drive, fileId: String): Result<Unit> =
        restoreBackupUseCase(driveService, fileId)

    suspend fun getLastBackupInfo(driveService: Drive): Result<BackupInfo?> =
        getLastBackupInfoUseCase(driveService)
}
