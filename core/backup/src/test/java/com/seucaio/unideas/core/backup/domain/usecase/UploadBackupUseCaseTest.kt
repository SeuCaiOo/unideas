package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class UploadBackupUseCaseTest {

    private val repository: BackupRepository = mockk()
    private val driveService: Drive = mockk()
    private val useCase = UploadBackupUseCase(repository)

    @Test
    fun `invoke returns the backup info from the repository`() = runTest {
        val expected = BackupInfo("file-id-1", LocalDateTime.now(), 1024L)
        coEvery { repository.uploadBackup(driveService, false) } returns Result.success(expected)

        val result = useCase(driveService)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.uploadBackup(driveService, false) }
    }

    @Test
    fun `invoke forwards isAutomatic to the repository`() = runTest {
        val expected = BackupInfo("file-id-1", LocalDateTime.now(), 1024L)
        coEvery { repository.uploadBackup(driveService, true) } returns Result.success(expected)

        val result = useCase(driveService, isAutomatic = true)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadBackup(driveService, true) }
    }

    @Test
    fun `invoke propagates a failure from the repository`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.uploadBackup(driveService, false) } returns Result.failure(error)

        val result = useCase(driveService)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
