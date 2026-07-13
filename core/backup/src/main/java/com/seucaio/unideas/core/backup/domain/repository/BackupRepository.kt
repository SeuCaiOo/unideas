package com.seucaio.unideas.core.backup.domain.repository

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

/** Uploads/lists/restores the raw Room database file on the Drive `appDataFolder`. */
interface BackupRepository {

    suspend fun uploadBackup(driveService: Drive): Result<BackupInfo>

    suspend fun listBackups(driveService: Drive): Result<List<BackupInfo>>

    suspend fun restoreBackup(driveService: Drive, fileId: String): Result<Unit>
}
