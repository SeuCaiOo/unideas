package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemLinkRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLinkedItemsUseCaseTest {

    private val repository: ItemLinkRepository = mockk()
    private val useCase = GetLinkedItemsUseCase(repository)

    @Test
    fun `invoke delegates the exact id to the repository`() = runTest {
        val items = listOf(ItemStub.task())
        every { repository.getLinkedItems(1L) } returns flowOf(items)

        val result = useCase(1L).first()

        assertEquals(items, result)
        verify(exactly = 1) { repository.getLinkedItems(1L) }
    }
}
