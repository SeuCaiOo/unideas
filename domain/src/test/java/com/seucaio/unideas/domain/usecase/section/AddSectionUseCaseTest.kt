package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSectionUseCaseTest {

    private val repository: SectionRepository = mockk()
    private val useCase = AddSectionUseCase(repository)

    @Test
    fun `invoke inserts a new section and returns the generated id`() = runTest {
        coEvery { repository.insertSection(Section(name = "Trabalho")) } returns 42L

        val result = useCase("Trabalho")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == 42L)
        coVerify(exactly = 1) { repository.insertSection(Section(name = "Trabalho")) }
    }

    @Test
    fun `invoke fails when name is blank and does not call the repository`() = runTest {
        val result = useCase(" ")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.insertSection(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        coEvery { repository.insertSection(Section(name = "Trabalho")) } throws IllegalStateException("boom")

        val result = useCase("Trabalho")

        assertTrue(result.isFailure)
    }
}
