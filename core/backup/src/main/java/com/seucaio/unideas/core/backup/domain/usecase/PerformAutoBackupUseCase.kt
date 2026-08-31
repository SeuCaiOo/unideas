package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import timber.log.Timber

/**
 * Uploads a new automatic backup and replaces whatever the previous one was — a single Drive
 * slot, never accumulating like the manually-triggered backups managed in the list (#184). A
 * no-op when the preference is off or no account is connected; a failure to delete the previous
 * slot doesn't fail the whole operation — the new backup already succeeded, a stray old file is
 * a minor cleanup gap, not worth losing the fresh upload over.
 */
class PerformAutoBackupUseCase(
    private val autoBackupRepository: AutoBackupRepository,
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val backupUseCase: BackupUseCase,
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!autoBackupRepository.isEnabled()) return@runCatching
        val account = googleAuthUseCase.getSignedInAccount() ?: return@runCatching

        val previousFileId = autoBackupRepository.getTrackedFileId()
        val uploaded = backupUseCase.upload(account, isAutomatic = true).getOrThrow()
        autoBackupRepository.setTrackedFileId(uploaded.fileId)

        if (previousFileId != null) {
            backupUseCase.delete(account, previousFileId)
                .onFailure { Timber.w(it, "Auto-backup: failed to delete previous slot $previousFileId") }
        }
    }
}
