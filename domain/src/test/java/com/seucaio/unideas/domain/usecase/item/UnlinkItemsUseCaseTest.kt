package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemLinkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnlinkItemsUseCaseTest {

    private val repository: ItemLinkRepository = mockk()
    private val useCase = UnlinkItemsUseCase(repository)

    @Test
    fun `invoke delegates the exact ids to the repository`() = runTest {
        coEvery { repository.unlinkItems(1L, 2L) } returns Unit

        val result = useCase(1L, 2L)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.unlinkItems(1L, 2L) }
    }

    @Test
    fun `invoke wraps a repository failure in Result`() = runTest {
        val error = IllegalStateException("boom")
        coEvery { repository.unlinkItems(1L, 2L) } throws error

        val result = useCase(1L, 2L)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
