package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemType
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

class GetItemsUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = GetItemsUseCase(repository)

    @Test
    fun `invoke delegates the exact type, sectionId and tagIds to the repository`() = runTest {
        val items = listOf(ItemStub.task())
        val tagIds = listOf(1L, 2L)
        every { repository.getItems(ItemType.TASK, 3L, tagIds) } returns flowOf(items)

        val result = useCase(type = ItemType.TASK, sectionId = 3L, tagIds = tagIds).first()

        assertEquals(items, result)
        verify(exactly = 1) { repository.getItems(ItemType.TASK, 3L, tagIds) }
    }

    @Test
    fun `invoke defaults pass no section and no tag filter`() = runTest {
        val items = listOf(ItemStub.note())
        every { repository.getItems(ItemType.NOTE, null, emptyList()) } returns flowOf(items)

        val result = useCase(type = ItemType.NOTE).first()

        assertEquals(items, result)
        verify(exactly = 1) { repository.getItems(ItemType.NOTE, null, emptyList()) }
    }
}
