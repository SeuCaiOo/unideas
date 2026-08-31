package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository

class GetBackupSyncStateUseCase(
    private val autoBackupRepository: AutoBackupRepository,
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase,
    private val getCurrentAutoBackupInfoUseCase: GetCurrentAutoBackupInfoUseCase,
) {

    suspend operator fun invoke(account: GoogleSignInAccount): Result<BackupSyncState> = runCatching {
        val driveService = buildDriveServiceUseCase(account)
        val remoteBackup = getCurrentAutoBackupInfoUseCase(driveService).getOrThrow()
            ?: return@runCatching BackupSyncState.NoRemoteBackup

        val localTrackedFileId = autoBackupRepository.getTrackedFileId()
        if (remoteBackup.fileId == localTrackedFileId) {
            BackupSyncState.Synced
        } else {
            BackupSyncState.Desynced(remoteBackup)
        }
    }
}
