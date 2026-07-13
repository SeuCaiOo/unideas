package com.seucaio.unideas.domain.usecase

import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSectionsAndTagsUseCaseTest {

    private val sectionUseCase: SectionUseCase = mockk()
    private val tagUseCase: TagUseCase = mockk()
    private val useCase = GetSectionsAndTagsUseCase(sectionUseCase, tagUseCase)

    @Test
    fun `invoke returns a snapshot with both lists`() = runTest {
        every { sectionUseCase.getAll() } returns flowOf(SectionStub.sections())
        every { tagUseCase.getAll() } returns flowOf(TagStub.tags())

        val result = useCase()

        assertEquals(SectionsAndTags(SectionStub.sections(), TagStub.tags()), result)
    }

    @Test
    fun `invoke falls back to an empty sections list when that flow throws`() = runTest {
        every { sectionUseCase.getAll() } returns flow { throw IllegalStateException("boom") }
        every { tagUseCase.getAll() } returns flowOf(TagStub.tags())

        val result = useCase()

        assertEquals(SectionsAndTags(emptyList(), TagStub.tags()), result)
    }

    @Test
    fun `invoke falls back to an empty tags list when that flow throws`() = runTest {
        every { sectionUseCase.getAll() } returns flowOf(SectionStub.sections())
        every { tagUseCase.getAll() } returns flow { throw IllegalStateException("boom") }

        val result = useCase()

        assertEquals(SectionsAndTags(SectionStub.sections(), emptyList()), result)
    }
}
