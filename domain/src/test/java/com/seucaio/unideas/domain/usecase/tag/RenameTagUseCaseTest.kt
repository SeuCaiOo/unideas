package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.repository.TagRepository
import com.seucaio.unideas.domain.stub.TagStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RenameTagUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = RenameTagUseCase(repository)

    @Test
    fun `invoke renames the tag`() = runTest {
        val tag = TagStub.tag(name = "renomeada")
        coEvery { repository.updateTag(tag) } returns Unit

        val result = useCase(tag)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateTag(tag) }
    }

    @Test
    fun `invoke fails when name is blank and does not call the repository`() = runTest {
        val tag = TagStub.tag(name = " ")

        val result = useCase(tag)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateTag(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val tag = TagStub.tag()
        coEvery { repository.updateTag(tag) } throws IllegalStateException("boom")

        val result = useCase(tag)

        assertTrue(result.isFailure)
    }
}
