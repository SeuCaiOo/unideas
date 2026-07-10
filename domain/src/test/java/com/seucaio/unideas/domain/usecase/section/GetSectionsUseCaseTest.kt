package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.stub.SectionStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSectionsUseCaseTest {

    private val repository: SectionRepository = mockk()
    private val useCase = GetSectionsUseCase(repository)

    @Test
    fun `invoke delegates to the repository and emits the sections`() = runTest {
        val sections = SectionStub.sections()
        every { repository.getSections() } returns flowOf(sections)

        val result = useCase().first()

        assertEquals(sections, result)
        verify(exactly = 1) { repository.getSections() }
    }
}
