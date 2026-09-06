package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import kotlinx.coroutines.delay

class GetConfirmedBackupSyncStateUseCase(
    private val getBackupSyncStateUseCase: GetBackupSyncStateUseCase,
) {

    suspend operator fun invoke(account: GoogleSignInAccount): BackupInfo? {
        val desynced =
            getBackupSyncStateUseCase(account).getOrNull() as? BackupSyncState.Desynced
                ?: return null
        delay(RECHECK_DELAY_MILLIS)
        val stillDesynced =
            getBackupSyncStateUseCase(account).getOrNull() is BackupSyncState.Desynced
        return desynced.remoteBackup.takeIf { stillDesynced }
    }

    private companion object {
        const val RECHECK_DELAY_MILLIS = 1500L
    }
}
