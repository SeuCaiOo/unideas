package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

/**
 * Facade over the backup-data use cases — takes a [GoogleSignInAccount] and builds the
 * `Drive` service itself before delegating, so callers (the ViewModel) never touch the
 * intermediate Drive service directly. Sign-in/session concerns (getting the intent, resolving
 * the current account) live in [GoogleAuthUseCase] instead.
 */
class BackupUseCase(
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase,
    private val uploadBackupUseCase: UploadBackupUseCase,
    private val listBackupsUseCase: ListBackupsUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val getLastBackupInfoUseCase: GetLastBackupInfoUseCase,
) {

    suspend fun upload(account: GoogleSignInAccount): Result<BackupInfo> =
        uploadBackupUseCase(buildDriveServiceUseCase(account))

    suspend fun list(account: GoogleSignInAccount): Result<List<BackupInfo>> =
        listBackupsUseCase(buildDriveServiceUseCase(account))

    suspend fun restore(account: GoogleSignInAccount, fileId: String): Result<Unit> =
        restoreBackupUseCase(buildDriveServiceUseCase(account), fileId)

    suspend fun getLastBackupInfo(account: GoogleSignInAccount): Result<BackupInfo?> =
        getLastBackupInfoUseCase(buildDriveServiceUseCase(account))
}
