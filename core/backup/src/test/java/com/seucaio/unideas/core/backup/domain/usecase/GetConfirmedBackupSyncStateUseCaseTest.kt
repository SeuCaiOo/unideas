package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class GetConfirmedBackupSyncStateUseCaseTest {

    private val getBackupSyncStateUseCase: GetBackupSyncStateUseCase = mockk()
    private val useCase = GetConfirmedBackupSyncStateUseCase(getBackupSyncStateUseCase)

    private val account: GoogleSignInAccount = mockk()
    private val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)

    @Test
    fun `invoke returns null when the first read is not desynced`() = runTest {
        coEvery { getBackupSyncStateUseCase(account) } returns Result.success(BackupSyncState.Synced)

        val result = useCase(account)

        assertNull(result)
    }

    @Test
    fun `invoke returns the remote backup when a desync holds on the recheck`() = runTest {
        coEvery { getBackupSyncStateUseCase(account) } returns Result.success(BackupSyncState.Desynced(remoteBackup))

        val result = useCase(account)

        assertEquals(remoteBackup, result)
    }

    @Test
    fun `invoke returns null when a desync on the first read resolves by the recheck`() = runTest {
        val results = listOf(
            Result.success(BackupSyncState.Desynced(remoteBackup)),
            Result.success(BackupSyncState.Synced),
        ).iterator()
        coEvery { getBackupSyncStateUseCase(account) } coAnswers { results.next() }

        val result = useCase(account)

        assertNull(result)
    }
}
