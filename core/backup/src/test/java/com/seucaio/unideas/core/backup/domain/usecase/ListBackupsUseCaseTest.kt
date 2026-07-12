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

class ListBackupsUseCaseTest {

    private val repository: BackupRepository = mockk()
    private val driveService: Drive = mockk()
    private val useCase = ListBackupsUseCase(repository)

    @Test
    fun `invoke returns the backup list from the repository`() = runTest {
        val backups = listOf(
            BackupInfo("id-1", LocalDateTime.now(), 512L),
            BackupInfo("id-2", LocalDateTime.now().minusDays(1), 490L),
        )
        coEvery { repository.listBackups(driveService) } returns Result.success(backups)

        val result = useCase(driveService)

        assertTrue(result.isSuccess)
        assertEquals(backups, result.getOrNull())
        coVerify(exactly = 1) { repository.listBackups(driveService) }
    }

    @Test
    fun `invoke returns an empty list when there are no backups`() = runTest {
        coEvery { repository.listBackups(driveService) } returns Result.success(emptyList())

        val result = useCase(driveService)

        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `invoke propagates a failure from the repository`() = runTest {
        coEvery { repository.listBackups(driveService) } returns Result.failure(RuntimeException("error"))

        val result = useCase(driveService)

        assertTrue(result.isFailure)
    }
}
