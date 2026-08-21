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

class GetArchivedItemsUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = GetArchivedItemsUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val items = listOf(ItemStub.task())
        every { repository.getArchivedItems() } returns flowOf(items)

        val result = useCase().first()

        assertEquals(items, result)
        verify(exactly = 1) { repository.getArchivedItems() }
    }
}
