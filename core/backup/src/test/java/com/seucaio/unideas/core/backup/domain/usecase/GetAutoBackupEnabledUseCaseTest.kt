package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAutoBackupEnabledUseCaseTest {

    private val repository: AutoBackupRepository = mockk()
    private val useCase = GetAutoBackupEnabledUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        coEvery { repository.isEnabled() } returns true

        val result = useCase()

        assertTrue(result)
        coVerify(exactly = 1) { repository.isEnabled() }
    }
}
