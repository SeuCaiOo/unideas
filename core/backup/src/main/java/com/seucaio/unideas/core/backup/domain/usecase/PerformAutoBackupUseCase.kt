package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import timber.log.Timber

class PerformAutoBackupUseCase(
    private val autoBackupRepository: AutoBackupRepository,
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val backupUseCase: BackupUseCase,
    private val getBackupSyncStateUseCase: GetBackupSyncStateUseCase,
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!autoBackupRepository.isEnabled()) return@runCatching
        val account = googleAuthUseCase.getSignedInAccount() ?: return@runCatching

        val syncState = getBackupSyncStateUseCase(account).getOrThrow()
        if (syncState is BackupSyncState.Desynced) {
            Timber.i("Auto-backup: SKIPPED upload, local tracked backup is out of sync with Drive")
            return@runCatching
        }

        val previousFileId = autoBackupRepository.getTrackedFileId()
        val uploaded = backupUseCase.upload(account, isAutomatic = true).getOrThrow()
        autoBackupRepository.setTrackedFileId(uploaded.fileId)
        Timber.i("Auto-backup: UPLOADED new automatic backup, fileId=${uploaded.fileId}")

        if (previousFileId != null) {
            backupUseCase.delete(account, previousFileId)
                .onFailure { Timber.w(it, "Auto-backup: failed to delete previous slot $previousFileId") }
        }
    }
}
