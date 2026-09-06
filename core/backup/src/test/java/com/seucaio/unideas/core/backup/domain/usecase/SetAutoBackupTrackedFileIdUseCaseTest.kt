package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetAutoBackupTrackedFileIdUseCaseTest {

    private val repository: AutoBackupRepository = mockk()
    private val useCase = SetAutoBackupTrackedFileIdUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        coEvery { repository.setTrackedFileId("file-1") } returns Unit

        useCase("file-1")

        coVerify(exactly = 1) { repository.setTrackedFileId("file-1") }
    }
}
