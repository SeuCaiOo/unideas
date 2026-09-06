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
    private val listBackupsUseCase: ListBackupsUseCase = mockk()
    private val useCase = GetBackupSyncStateUseCase(
        autoBackupRepository,
        buildDriveServiceUseCase,
        listBackupsUseCase,
    )

    private val account: GoogleSignInAccount = mockk()
    private val driveService: Drive = mockk()

    @Before
    fun setUp() {
        every { buildDriveServiceUseCase(account) } returns driveService
    }

    @Test
    fun `invoke returns Synced when the tracked file id matches the most recent remote backup`() = runTest {
        val remoteBackup = BackupInfo("same-file", LocalDateTime.now(), 2048L)
        coEvery { listBackupsUseCase(driveService) } returns Result.success(listOf(remoteBackup))
        coEvery { autoBackupRepository.getTrackedFileId() } returns "same-file"

        val result = useCase(account)

        assertEquals(BackupSyncState.Synced, result.getOrNull())
    }

    @Test
    fun `invoke returns Desynced when the tracked file id differs from the most recent remote backup`() = runTest {
        val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)
        coEvery { listBackupsUseCase(driveService) } returns Result.success(listOf(remoteBackup))
        coEvery { autoBackupRepository.getTrackedFileId() } returns "local-file"

        val result = useCase(account)

        assertEquals(BackupSyncState.Desynced(remoteBackup), result.getOrNull())
    }

    @Test
    fun `invoke returns Desynced for a manual backup made on another device, same as an automatic one`() = runTest {
        val manualRemoteBackup = BackupInfo("manual-remote-file", LocalDateTime.now(), 2048L)
        coEvery { listBackupsUseCase(driveService) } returns Result.success(listOf(manualRemoteBackup))
        coEvery { autoBackupRepository.getTrackedFileId() } returns "local-file"

        val result = useCase(account)

        assertEquals(BackupSyncState.Desynced(manualRemoteBackup), result.getOrNull())
    }

    @Test
    fun `invoke returns Desynced when this device never tracked a backup but one exists remotely`() = runTest {
        val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)
        coEvery { listBackupsUseCase(driveService) } returns Result.success(listOf(remoteBackup))
        coEvery { autoBackupRepository.getTrackedFileId() } returns null

        val result = useCase(account)

        assertEquals(BackupSyncState.Desynced(remoteBackup), result.getOrNull())
    }

    @Test
    fun `invoke returns NoRemoteBackup when there is no backup on Drive`() = runTest {
        coEvery { listBackupsUseCase(driveService) } returns Result.success(emptyList())

        val result = useCase(account)

        assertEquals(BackupSyncState.NoRemoteBackup, result.getOrNull())
    }

    @Test
    fun `invoke propagates a failure from listBackups`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { listBackupsUseCase(driveService) } returns Result.failure(error)

        val result = useCase(account)

        assertEquals(error, result.exceptionOrNull())
    }
}
