package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetItemArchivedUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = SetItemArchivedUseCase(repository)

    @Test
    fun `invoke archived true sets status ARCHIVED`() = runTest {
        coEvery { repository.setItemStatus(7L, ItemStatus.ARCHIVED) } returns Unit

        val result = useCase(7L, true)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.setItemStatus(7L, ItemStatus.ARCHIVED) }
    }

    @Test
    fun `invoke archived false sets status ACTIVE`() = runTest {
        coEvery { repository.setItemStatus(7L, ItemStatus.ACTIVE) } returns Unit

        val result = useCase(7L, false)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.setItemStatus(7L, ItemStatus.ACTIVE) }
    }

    @Test
    fun `invoke wraps a repository failure in Result`() = runTest {
        val error = IllegalStateException("boom")
        coEvery { repository.setItemStatus(7L, ItemStatus.ARCHIVED) } throws error

        val result = useCase(7L, true)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
