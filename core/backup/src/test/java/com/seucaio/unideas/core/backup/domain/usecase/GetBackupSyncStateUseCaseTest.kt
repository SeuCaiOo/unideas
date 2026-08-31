package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.model.BackupSyncState
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class GetBackupSyncStateUseCaseTest {

    private val autoBackupRepository: AutoBackupRepository = mockk()
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase = mockk()
    private val getCurrentAutoBackupInfoUseCase: GetCurrentAutoBackupInfoUseCase = mockk()
    private val useCase = GetBackupSyncStateUseCase(
        autoBackupRepository,
        buildDriveServiceUseCase,
        getCurrentAutoBackupInfoUseCase,
    )

    private val account: GoogleSignInAccount = mockk()
    private val driveService: Drive = mockk()

    @Before
    fun setUp() {
        every { buildDriveServiceUseCase(account) } returns driveService
    }

    @Test
    fun `invoke returns Synced when the tracked file id matches the remote automatic backup`() = runTest {
        val remoteBackup = BackupInfo("same-file", LocalDateTime.now(), 2048L)
        coEvery { getCurrentAutoBackupInfoUseCase(driveService) } returns Result.success(remoteBackup)
        coEvery { autoBackupRepository.getTrackedFileId() } returns "same-file"

        val result = useCase(account)

        assertEquals(BackupSyncState.Synced, result.getOrNull())
    }

    @Test
    fun `invoke returns Desynced when the tracked file id differs from the remote automatic backup`() = runTest {
        val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)
        coEvery { getCurrentAutoBackupInfoUseCase(driveService) } returns Result.success(remoteBackup)
        coEvery { autoBackupRepository.getTrackedFileId() } returns "local-file"

        val result = useCase(account)

        assertEquals(BackupSyncState.Desynced(remoteBackup), result.getOrNull())
    }

    @Test
    fun `invoke returns Desynced when this device never tracked a backup but one exists remotely`() = runTest {
        val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)
        coEvery { getCurrentAutoBackupInfoUseCase(driveService) } returns Result.success(remoteBackup)
        coEvery { autoBackupRepository.getTrackedFileId() } returns null

        val result = useCase(account)

        assertEquals(BackupSyncState.Desynced(remoteBackup), result.getOrNull())
    }

    @Test
    fun `invoke returns NoRemoteBackup when there is no automatic backup on Drive`() = runTest {
        coEvery { getCurrentAutoBackupInfoUseCase(driveService) } returns Result.success(null)

        val result = useCase(account)

        assertEquals(BackupSyncState.NoRemoteBackup, result.getOrNull())
    }

    @Test
    fun `invoke propagates a failure from getCurrentAutoBackupInfo`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { getCurrentAutoBackupInfoUseCase(driveService) } returns Result.failure(error)

        val result = useCase(account)

        assertEquals(error, result.exceptionOrNull())
    }
}
