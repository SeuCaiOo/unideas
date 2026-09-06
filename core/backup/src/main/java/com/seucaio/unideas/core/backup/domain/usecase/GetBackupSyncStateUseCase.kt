package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import timber.log.Timber

class GetBackupSyncStateUseCase(
    private val autoBackupRepository: AutoBackupRepository,
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase,
    private val listBackupsUseCase: ListBackupsUseCase,
) {

    suspend operator fun invoke(account: GoogleSignInAccount): Result<BackupSyncState> = runCatching {
        val driveService = buildDriveServiceUseCase(account)
        val remoteBackup = listBackupsUseCase(driveService).getOrThrow().firstOrNull()
            ?: return@runCatching BackupSyncState.NoRemoteBackup.also {
                Timber.i("Backup sync check: no backup found on Drive")
            }

        val localTrackedFileId = autoBackupRepository.getTrackedFileId()
        Timber.i(
            "Backup sync check: local tracked fileId=$localTrackedFileId, " +
                "remote current fileId=${remoteBackup.fileId}",
        )
        if (remoteBackup.fileId == localTrackedFileId) {
            BackupSyncState.Synced
        } else {
            BackupSyncState.Desynced(remoteBackup)
        }
    }
}
