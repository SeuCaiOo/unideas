package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetAutoBackupEnabledUseCaseTest {

    private val repository: AutoBackupRepository = mockk()
    private val useCase = SetAutoBackupEnabledUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        coEvery { repository.setEnabled(true) } returns Unit

        useCase(true)

        coVerify(exactly = 1) { repository.setEnabled(true) }
    }
}
