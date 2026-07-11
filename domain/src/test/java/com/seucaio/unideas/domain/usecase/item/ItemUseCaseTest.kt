package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.model.outcome.CompletionResult
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
import java.time.LocalDateTime

/** [ItemUseCase] is a delegating facade — these tests only check the delegation itself. */
class ItemUseCaseTest {

    private val getItemDetail: GetItemDetailUseCase = mockk()
    private val deleteItem: DeleteItemUseCase = mockk()
    private val completeItem: CompleteItemUseCase = mockk()
    private val useCase = ItemUseCase(getItemDetail, deleteItem, completeItem)

    @Test
    fun `getDetail delegates to GetItemDetailUseCase`() = runTest {
        val detail = ItemDetail(item = ItemStub.task(), sectionName = "Trabalho")
        every { getItemDetail(1L) } returns flowOf(detail)

        val result = useCase.getDetail(1L).first()

        assertEquals(detail, result)
        verify(exactly = 1) { getItemDetail(1L) }
    }

    @Test
    fun `delete delegates to DeleteItemUseCase`() = runTest {
        coEvery { deleteItem(1L) } returns Unit

        useCase.delete(1L)

        coVerify(exactly = 1) { deleteItem(1L) }
    }

    @Test
    fun `complete delegates to CompleteItemUseCase`() = runTest {
        val item = ItemStub.task()
        val now = LocalDateTime.of(2026, 7, 1, 10, 0)
        coEvery { completeItem(item, now) } returns Result.success(CompletionResult.Completed)

        val result = useCase.complete(item, now)

        assertEquals(CompletionResult.Completed, result.getOrNull())
        coVerify(exactly = 1) { completeItem(item, now) }
    }
}
