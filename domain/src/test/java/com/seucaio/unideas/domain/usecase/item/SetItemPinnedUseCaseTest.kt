package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetItemPinnedUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = SetItemPinnedUseCase(repository)

    @Test
    fun `invoke delegates the exact id and pinned flag to the repository`() = runTest {
        coEvery { repository.setItemPinned(7L, true) } returns Unit

        val result = useCase(7L, true)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.setItemPinned(7L, true) }
    }

    @Test
    fun `invoke wraps a repository failure in Result`() = runTest {
        val error = IllegalStateException("boom")
        coEvery { repository.setItemPinned(7L, true) } throws error

        val result = useCase(7L, true)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
