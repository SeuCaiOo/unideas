package com.seucaio.unideas.domain.usecase.settings

import com.seucaio.unideas.domain.model.SeedScope
import com.seucaio.unideas.domain.repository.DatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SeedDatabaseUseCaseTest {

    private val repository: DatabaseRepository = mockk()
    private val useCase = SeedDatabaseUseCase(repository)

    @Test
    fun `invoke delegates the scope to the repository`() = runTest {
        coEvery { repository.seed(SeedScope.FULL) } returns Unit

        useCase(SeedScope.FULL)

        coVerify(exactly = 1) { repository.seed(SeedScope.FULL) }
    }
}
