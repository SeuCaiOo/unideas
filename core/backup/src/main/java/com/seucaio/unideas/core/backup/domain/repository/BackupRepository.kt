package com.seucaio.unideas.core.backup.domain.repository

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

interface BackupRepository {

    suspend fun uploadBackup(driveService: Drive, isAutomatic: Boolean = false): Result<BackupInfo>

    suspend fun listBackups(driveService: Drive): Result<List<BackupInfo>>

    suspend fun getCurrentAutoBackupInfo(driveService: Drive): Result<BackupInfo?>

    suspend fun restoreBackup(driveService: Drive, fileId: String): Result<Unit>

    suspend fun deleteBackup(driveService: Drive, fileId: String): Result<Unit>
}
