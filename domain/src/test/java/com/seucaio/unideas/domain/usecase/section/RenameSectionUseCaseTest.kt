package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.stub.SectionStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RenameSectionUseCaseTest {

    private val repository: SectionRepository = mockk()
    private val useCase = RenameSectionUseCase(repository)

    @Test
    fun `invoke renames the section`() = runTest {
        val section = SectionStub.section(name = "renomeada")
        coEvery { repository.updateSection(section) } returns Unit

        val result = useCase(section)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateSection(section) }
    }

    @Test
    fun `invoke fails when name is blank and does not call the repository`() = runTest {
        val section = SectionStub.section(name = " ")

        val result = useCase(section)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateSection(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val section = SectionStub.section()
        coEvery { repository.updateSection(section) } throws IllegalStateException("boom")

        val result = useCase(section)

        assertTrue(result.isFailure)
    }
}
