package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.SectionStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** [SectionUseCase] is a delegating facade — these tests only check the delegation itself. */
class SectionUseCaseTest {

    private val getSections: GetSectionsUseCase = mockk()
    private val addSection: AddSectionUseCase = mockk()
    private val renameSection: RenameSectionUseCase = mockk()
    private val deleteSection: DeleteSectionUseCase = mockk()
    private val useCase = SectionUseCase(getSections, addSection, renameSection, deleteSection)

    @Test
    fun `getAll delegates to GetSectionsUseCase`() = runTest {
        val sections = SectionStub.sections()
        every { getSections() } returns flowOf(sections)

        val result = useCase.getAll().first()

        assertEquals(sections, result)
        verify(exactly = 1) { getSections() }
    }

    @Test
    fun `add delegates to AddSectionUseCase`() = runTest {
        coEvery { addSection("Trabalho") } returns Result.success(1L)

        val result = useCase.add("Trabalho")

        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { addSection("Trabalho") }
    }

    @Test
    fun `rename delegates to RenameSectionUseCase`() = runTest {
        val section = SectionStub.section(name = "renomeada")
        coEvery { renameSection(section) } returns Result.success(Unit)

        val result = useCase.rename(section)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { renameSection(section) }
    }

    @Test
    fun `delete delegates to DeleteSectionUseCase`() = runTest {
        coEvery { deleteSection(1L) } returns Result.success(DeletionStatus.Deleted)

        val result = useCase.delete(1L)

        assertEquals(DeletionStatus.Deleted, result.getOrNull())
        coVerify(exactly = 1) { deleteSection(1L) }
    }
}
