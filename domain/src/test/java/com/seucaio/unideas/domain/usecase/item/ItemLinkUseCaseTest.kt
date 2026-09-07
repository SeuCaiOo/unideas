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

/** [ItemLinkUseCase] is a delegating facade — these tests only check the delegation itself. */
class ItemLinkUseCaseTest {

    private val getLinkedItems: GetLinkedItemsUseCase = mockk()
    private val linkItems: LinkItemsUseCase = mockk()
    private val unlinkItems: UnlinkItemsUseCase = mockk()
    private val useCase = ItemLinkUseCase(getLinkedItems, linkItems, unlinkItems)

    @Test
    fun `getLinkedItems delegates to GetLinkedItemsUseCase`() = runTest {
        val items = listOf(ItemStub.task(id = 2L))
        every { getLinkedItems(1L) } returns flowOf(items)

        val result = useCase.getLinkedItems(1L).first()

        assertEquals(items, result)
        verify(exactly = 1) { getLinkedItems(1L) }
    }

    @Test
    fun `link delegates to LinkItemsUseCase`() = runTest {
        coEvery { linkItems(1L, 2L) } returns Result.success(Unit)

        val result = useCase.link(1L, 2L)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { linkItems(1L, 2L) }
    }

    @Test
    fun `unlink delegates to UnlinkItemsUseCase`() = runTest {
        coEvery { unlinkItems(1L, 2L) } returns Result.success(Unit)

        val result = useCase.unlink(1L, 2L)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { unlinkItems(1L, 2L) }
    }
}
