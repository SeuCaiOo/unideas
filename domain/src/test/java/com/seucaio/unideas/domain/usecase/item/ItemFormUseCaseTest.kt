package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ItemFormUseCase] is a delegating facade — these tests only check the delegation itself. */
class ItemFormUseCaseTest {

    private val getItem: GetItemUseCase = mockk()
    private val createItem: CreateItemUseCase = mockk()
    private val editItem: EditItemUseCase = mockk()
    private val useCase = ItemFormUseCase(getItem, createItem, editItem)

    @Test
    fun `get delegates to GetItemUseCase`() = runTest {
        val item = ItemStub.task()
        every { getItem(1L) } returns flowOf(item)

        val result = useCase.get(1L).first()

        assertEquals(item, result)
        verify(exactly = 1) { getItem(1L) }
    }

    @Test
    fun `create delegates to CreateItemUseCase`() = runTest {
        val item = ItemStub.task(id = 0L)
        coEvery { createItem(item) } returns Result.success(42L)

        val result = useCase.create(item)

        assertEquals(42L, result.getOrNull())
        coVerify(exactly = 1) { createItem(item) }
    }

    @Test
    fun `edit delegates to EditItemUseCase`() = runTest {
        val item = ItemStub.task()
        coEvery { editItem(item) } returns Result.success(Unit)

        val result = useCase.edit(item)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { editItem(item) }
    }
}
