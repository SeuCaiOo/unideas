package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.repository.TagRepository
import com.seucaio.unideas.domain.stub.TagStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTagsUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = GetTagsUseCase(repository)

    @Test
    fun `invoke delegates to the repository and emits the tags`() = runTest {
        val tags = TagStub.tags()
        every { repository.getTags() } returns flowOf(tags)

        val result = useCase().first()

        assertEquals(tags, result)
        verify(exactly = 1) { repository.getTags() }
    }
}
