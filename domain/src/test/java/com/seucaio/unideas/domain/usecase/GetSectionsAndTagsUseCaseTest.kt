package com.seucaio.unideas.domain.usecase

import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSectionsAndTagsUseCaseTest {

    private val getSections: GetSectionsUseCase = mockk()
    private val getTags: GetTagsUseCase = mockk()
    private val useCase = GetSectionsAndTagsUseCase(getSections, getTags)

    @Test
    fun `invoke returns a snapshot with both lists`() = runTest {
        every { getSections() } returns flowOf(SectionStub.sections())
        every { getTags() } returns flowOf(TagStub.tags())

        val result = useCase()

        assertEquals(SectionsAndTags(SectionStub.sections(), TagStub.tags()), result)
    }

    @Test
    fun `invoke falls back to an empty sections list when that flow throws`() = runTest {
        every { getSections() } returns flow { throw IllegalStateException("boom") }
        every { getTags() } returns flowOf(TagStub.tags())

        val result = useCase()

        assertEquals(SectionsAndTags(emptyList(), TagStub.tags()), result)
    }

    @Test
    fun `invoke falls back to an empty tags list when that flow throws`() = runTest {
        every { getSections() } returns flowOf(SectionStub.sections())
        every { getTags() } returns flow { throw IllegalStateException("boom") }

        val result = useCase()

        assertEquals(SectionsAndTags(SectionStub.sections(), emptyList()), result)
    }
}
