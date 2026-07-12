package com.seucaio.unideas.domain.usecase.settings

import com.seucaio.unideas.domain.repository.DatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearDatabaseUseCaseTest {

    private val repository: DatabaseRepository = mockk()
    private val useCase = ClearDatabaseUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        coEvery { repository.clearAll() } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.clearAll() }
    }
}
