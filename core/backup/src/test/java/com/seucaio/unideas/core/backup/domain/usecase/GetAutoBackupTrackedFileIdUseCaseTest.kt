package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAutoBackupTrackedFileIdUseCaseTest {

    private val repository: AutoBackupRepository = mockk()
    private val useCase = GetAutoBackupTrackedFileIdUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        coEvery { repository.getTrackedFileId() } returns "file-1"

        val result = useCase()

        assertEquals("file-1", result)
        coVerify(exactly = 1) { repository.getTrackedFileId() }
    }
}
