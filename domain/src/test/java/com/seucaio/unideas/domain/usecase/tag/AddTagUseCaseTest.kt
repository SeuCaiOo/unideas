package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTagUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = AddTagUseCase(repository)

    @Test
    fun `invoke inserts a new tag and returns the generated id`() = runTest {
        coEvery { repository.insertTag(Tag(name = "urgente")) } returns 42L

        val result = useCase("urgente")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == 42L)
        coVerify(exactly = 1) { repository.insertTag(Tag(name = "urgente")) }
    }

    @Test
    fun `invoke fails when name is blank and does not call the repository`() = runTest {
        val result = useCase(" ")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.insertTag(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        coEvery { repository.insertTag(Tag(name = "urgente")) } throws IllegalStateException("boom")

        val result = useCase("urgente")

        assertTrue(result.isFailure)
    }
}
