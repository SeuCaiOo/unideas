package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

/**
 * Convenience facade over the single-purpose backup use cases (kept as-is, still usable on
 * their own) — one method per operation, each just delegating, no repository access here.
 * All 5 are specific to backup and only consumed by `BackupViewModel`, so everything is
 * folded in (unlike [com.seucaio.unideas.domain.usecase.item.HomeUseCase], which keeps a
 * cross-entity use case out).
 */
class BackupUseCase(
    private val getSignInIntentUseCase: GetSignInIntentUseCase,
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase,
    private val uploadBackupUseCase: UploadBackupUseCase,
    private val listBackupsUseCase: ListBackupsUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val getLastBackupInfoUseCase: GetLastBackupInfoUseCase,
) {

    fun getSignInIntent(): Intent = getSignInIntentUseCase()

    fun buildDriveService(account: GoogleSignInAccount): Drive = buildDriveServiceUseCase(account)

    suspend fun upload(driveService: Drive): Result<BackupInfo> = uploadBackupUseCase(driveService)

    suspend fun list(driveService: Drive): Result<List<BackupInfo>> = listBackupsUseCase(driveService)

    suspend fun restore(driveService: Drive, fileId: String): Result<Unit> =
        restoreBackupUseCase(driveService, fileId)

    suspend fun getLastBackupInfo(driveService: Drive): Result<BackupInfo?> =
        getLastBackupInfoUseCase(driveService)
}
