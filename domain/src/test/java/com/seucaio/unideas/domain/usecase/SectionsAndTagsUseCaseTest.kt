package com.seucaio.unideas.domain.usecase

import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SectionsAndTagsUseCaseTest {

    private val sectionUseCase: SectionUseCase = mockk()
    private val tagUseCase: TagUseCase = mockk()
    private val useCase = SectionsAndTagsUseCase(sectionUseCase, tagUseCase)

    @Test
    fun `getAll combines both lists`() = runTest {
        every { sectionUseCase.getAll() } returns flowOf(SectionStub.sections())
        every { tagUseCase.getAll() } returns flowOf(TagStub.tags())

        val result = useCase.getAll().first()

        assertEquals(SectionsAndTags(SectionStub.sections(), TagStub.tags()), result)
    }

    @Test
    fun `getAll falls back to an empty sections list when that flow throws`() = runTest {
        every { sectionUseCase.getAll() } returns flow { throw IllegalStateException("boom") }
        every { tagUseCase.getAll() } returns flowOf(TagStub.tags())

        val result = useCase.getAll().first()

        assertEquals(SectionsAndTags(emptyList(), TagStub.tags()), result)
    }

    @Test
    fun `getAll falls back to an empty tags list when that flow throws`() = runTest {
        every { sectionUseCase.getAll() } returns flowOf(SectionStub.sections())
        every { tagUseCase.getAll() } returns flow { throw IllegalStateException("boom") }

        val result = useCase.getAll().first()

        assertEquals(SectionsAndTags(SectionStub.sections(), emptyList()), result)
    }

    @Test
    fun `addSection delegates to SectionUseCase's add`() = runTest {
        coEvery { sectionUseCase.add("Casa") } returns Result.success(1L)

        val result = useCase.addSection("Casa")

        assertEquals(Result.success(1L), result)
    }

    @Test
    fun `addTag delegates to TagUseCase's add`() = runTest {
        coEvery { tagUseCase.add("Urgente") } returns Result.success(1L)

        val result = useCase.addTag("Urgente")

        assertEquals(Result.success(1L), result)
    }
}
