package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreBackupUseCaseTest {

    private val repository: BackupRepository = mockk()
    private val driveService: Drive = mockk()
    private val useCase = RestoreBackupUseCase(repository)

    @Test
    fun `invoke delegates the file id to the repository`() = runTest {
        coEvery { repository.restoreBackup(driveService, "file-id-1") } returns Result.success(Unit)

        val result = useCase(driveService, "file-id-1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.restoreBackup(driveService, "file-id-1") }
    }

    @Test
    fun `invoke propagates a failure from the repository`() = runTest {
        coEvery { repository.restoreBackup(driveService, "file-id-1") } returns
            Result.failure(RuntimeException("IO error"))

        val result = useCase(driveService, "file-id-1")

        assertTrue(result.isFailure)
    }
}
