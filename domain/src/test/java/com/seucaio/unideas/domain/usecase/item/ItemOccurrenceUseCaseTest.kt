package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
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

class ItemOccurrenceUseCaseTest {

    private val completeItem: CompleteItemUseCase = mockk()
    private val itemCompletionHistoryUseCase: ItemCompletionHistoryUseCase = mockk()
    private val ignoreOccurrenceUseCase: IgnoreOccurrenceUseCase = mockk()
    private val extendItemDueDateUseCase: ExtendItemDueDateUseCase = mockk()
    private val useCase = ItemOccurrenceUseCase(
        completeItem,
        itemCompletionHistoryUseCase,
        ignoreOccurrenceUseCase,
        extendItemDueDateUseCase,
    )

    @Test
    fun `complete delegates to CompleteItemUseCase`() = runTest {
        val item = ItemStub.task()
        val completedAt = LocalDateTime.of(2026, 7, 23, 10, 0)
        coEvery {
            completeItem(
                item,
                completedAt,
                null
            )
        } returns Result.success(CompletionResult.Completed)

        val result = useCase.complete(item, completedAt)

        assertEquals(Result.success(CompletionResult.Completed), result)
        coVerify(exactly = 1) { completeItem(item, completedAt, null) }
    }

    @Test
    fun `complete passes note through to CompleteItemUseCase`() = runTest {
        val item = ItemStub.task()
        val completedAt = LocalDateTime.of(2026, 7, 23, 10, 0)
        val note = "Sem internet"
        coEvery {
            completeItem(
                item,
                completedAt,
                note
            )
        } returns Result.success(CompletionResult.Completed)

        val result = useCase.complete(item, completedAt, note)

        assertEquals(Result.success(CompletionResult.Completed), result)
        coVerify(exactly = 1) { completeItem(item, completedAt, note) }
    }

    @Test
    fun `getHistory delegates to ItemCompletionHistoryUseCase`() = runTest {
        val history = listOf(
            ItemCompletionHistory(itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null),
        )
        every { itemCompletionHistoryUseCase.getHistory(1L) } returns flowOf(history)

        val result = useCase.getHistory(1L).first()

        assertEquals(history, result)
        verify(exactly = 1) { itemCompletionHistoryUseCase.getHistory(1L) }
    }

    @Test
    fun `ignore delegates to IgnoreOccurrenceUseCase`() = runTest {
        val item = ItemStub.recurringTask()
        val note = "Não pude ler esses dias"
        val advanced = item.copy(dueDate = ItemStub.TODAY.plusWeeks(1))
        coEvery { ignoreOccurrenceUseCase(item, note, ItemStub.TODAY) } returns Result.success(
            advanced
        )

        val result = useCase.ignore(item, note, ItemStub.TODAY)

        assertEquals(Result.success(advanced), result)
        coVerify(exactly = 1) { ignoreOccurrenceUseCase(item, note, ItemStub.TODAY) }
    }

    @Test
    fun `extendDueDate delegates to ExtendItemDueDateUseCase`() = runTest {
        val item = ItemStub.overdueTask()
        val newDueDate = ItemStub.TODAY.plusDays(5)
        val extended = item.copy(dueDate = newDueDate)
        coEvery {
            extendItemDueDateUseCase(
                item,
                newDueDate,
                ItemStub.TODAY
            )
        } returns Result.success(extended)

        val result = useCase.extendDueDate(item, newDueDate, ItemStub.TODAY)

        assertEquals(Result.success(extended), result)
        coVerify(exactly = 1) { extendItemDueDateUseCase(item, newDueDate, ItemStub.TODAY) }
    }

    @Test
    fun `saveHistoryEntry delegates to ItemCompletionHistoryUseCase`() = runTest {
        val record = ItemCompletionHistory(itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null)
        coEvery { itemCompletionHistoryUseCase.save(record) } returns Result.success(Unit)

        val result = useCase.saveHistoryEntry(record)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { itemCompletionHistoryUseCase.save(record) }
    }

    @Test
    fun `deleteHistoryEntry delegates to ItemCompletionHistoryUseCase`() = runTest {
        coEvery { itemCompletionHistoryUseCase.delete(5L) } returns Result.success(Unit)

        val result = useCase.deleteHistoryEntry(5L)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { itemCompletionHistoryUseCase.delete(5L) }
    }
}
