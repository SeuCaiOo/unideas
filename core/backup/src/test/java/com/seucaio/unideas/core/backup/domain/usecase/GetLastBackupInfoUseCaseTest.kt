package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class GetLastBackupInfoUseCaseTest {

    private val listBackupsUseCase: ListBackupsUseCase = mockk()
    private val driveService: Drive = mockk()
    private val useCase = GetLastBackupInfoUseCase(listBackupsUseCase)

    @Test
    fun `invoke returns the most recent backup`() = runTest {
        val mostRecent = BackupInfo("id-1", LocalDateTime.now(), 512L)
        val older = BackupInfo("id-2", LocalDateTime.now().minusDays(1), 490L)
        coEvery { listBackupsUseCase(driveService) } returns Result.success(listOf(mostRecent, older))

        val result = useCase(driveService)

        assertEquals(mostRecent, result.getOrNull())
        coVerify(exactly = 1) { listBackupsUseCase(driveService) }
    }

    @Test
    fun `invoke returns null when there are no backups`() = runTest {
        coEvery { listBackupsUseCase(driveService) } returns Result.success(emptyList())

        val result = useCase(driveService)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `invoke propagates a failure from the underlying use case`() = runTest {
        coEvery { listBackupsUseCase(driveService) } returns Result.failure(RuntimeException("error"))

        val result = useCase(driveService)

        assertTrue(result.isFailure)
    }
}
