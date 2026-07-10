package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPriorityItemsUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = GetPriorityItemsUseCase(repository)
    private val today = ItemStub.TODAY

    @Test
    fun `invoke delegates the computed threshold to the repository`() = runTest {
        val items = listOf(ItemStub.overdueTask())
        every { repository.getPriorityItems(today.plusDays(3)) } returns flowOf(items)

        val result = useCase(today = today, dueSoonDays = 3, limit = 10).first()

        assertEquals(items, result)
        verify(exactly = 1) { repository.getPriorityItems(today.plusDays(3)) }
    }

    @Test
    fun `invoke caps the result at the given limit`() = runTest {
        val items = (1..5).map { ItemStub.overdueTask(id = it.toLong()) }
        every { repository.getPriorityItems(today.plusDays(3)) } returns flowOf(items)

        val result = useCase(today = today, dueSoonDays = 3, limit = 2).first()

        assertEquals(items.take(2), result)
    }
}
