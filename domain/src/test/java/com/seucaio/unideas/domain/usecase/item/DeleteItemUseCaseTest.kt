package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = DeleteItemUseCase(repository)

    @Test
    fun `invoke delegates the exact id to the repository`() = runTest {
        coEvery { repository.deleteItem(7L) } returns Unit

        val result = useCase(7L)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.deleteItem(7L) }
    }

    @Test
    fun `invoke wraps a repository failure in Result`() = runTest {
        val error = IllegalStateException("boom")
        coEvery { repository.deleteItem(7L) } throws error

        val result = useCase(7L)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
