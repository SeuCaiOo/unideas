package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SectionCrudOperationsTest {

    @MockK
    private lateinit var getSections: GetSectionsUseCase

    @MockK
    private lateinit var addSection: AddSectionUseCase

    @MockK
    private lateinit var renameSection: RenameSectionUseCase

    @MockK
    private lateinit var deleteSection: DeleteSectionUseCase

    private lateinit var operations: SectionCrudOperations

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        operations = SectionCrudOperations(getSections, addSection, renameSection, deleteSection)
    }

    @Test
    fun `getAll delegates to GetSectionsUseCase`() = runTest {
        val sections = SectionStub.sections()
        every { getSections() } returns flowOf(sections)

        assertEquals(sections, operations.getAll().first())
    }

    @Test
    fun `add delegates to AddSectionUseCase`() = runTest {
        coEvery { addSection("Trabalho") } returns Result.success(1L)

        val result = operations.add("Trabalho")

        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { addSection("Trabalho") }
    }

    @Test
    fun `rename copies the new name onto the section before delegating`() = runTest {
        val section = SectionStub.section(name = "Antigo")
        val renamed = section.copy(name = "Novo")
        coEvery { renameSection(renamed) } returns Result.success(Unit)

        operations.rename(section, "Novo")

        coVerify(exactly = 1) { renameSection(renamed) }
    }

    @Test
    fun `delete delegates to DeleteSectionUseCase with the section id`() = runTest {
        val section = SectionStub.section(id = 5L)
        coEvery { deleteSection(5L) } returns Result.success(DeletionStatus.Deleted)

        val result = operations.delete(section)

        assertEquals(DeletionStatus.Deleted, result.getOrNull())
        coVerify(exactly = 1) { deleteSection(5L) }
    }
}
