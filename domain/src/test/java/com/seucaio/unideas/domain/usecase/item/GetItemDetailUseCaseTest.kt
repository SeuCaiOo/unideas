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
import org.junit.Assert.assertNull
import org.junit.Test

class GetItemDetailUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = GetItemDetailUseCase(repository)

    @Test
    fun `invoke delegates to the repository and emits the item`() = runTest {
        val item = ItemStub.task()
        every { repository.getItemById(item.id) } returns flowOf(item)

        val result = useCase(item.id).first()

        assertEquals(item, result)
        verify(exactly = 1) { repository.getItemById(item.id) }
    }

    @Test
    fun `invoke emits null when the item does not exist`() = runTest {
        every { repository.getItemById(99L) } returns flowOf(null)

        val result = useCase(99L).first()

        assertNull(result)
    }
}
