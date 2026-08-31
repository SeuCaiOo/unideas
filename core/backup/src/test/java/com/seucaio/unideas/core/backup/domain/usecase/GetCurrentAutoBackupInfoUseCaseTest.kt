package com.seucaio.unideas.core.backup.domain.usecase

import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GetCurrentAutoBackupInfoUseCaseTest {

    private val repository: BackupRepository = mockk()
    private val driveService: Drive = mockk()
    private val useCase = GetCurrentAutoBackupInfoUseCase(repository)

    @Test
    fun `invoke returns the auto backup info from the repository`() = runTest {
        val expected = BackupInfo("auto-file-id", LocalDateTime.now(), 2048L)
        coEvery { repository.getCurrentAutoBackupInfo(driveService) } returns Result.success(expected)

        val result = useCase(driveService)

        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.getCurrentAutoBackupInfo(driveService) }
    }

    @Test
    fun `invoke returns null when the repository finds no automatic backup`() = runTest {
        coEvery { repository.getCurrentAutoBackupInfo(driveService) } returns Result.success(null)

        val result = useCase(driveService)

        assertEquals(null, result.getOrNull())
    }
}
