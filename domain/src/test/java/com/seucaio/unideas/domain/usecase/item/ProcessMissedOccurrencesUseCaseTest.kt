package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessMissedOccurrencesUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val historyRepository: ItemCompletionHistoryRepository = mockk()
    private val useCase = ProcessMissedOccurrencesUseCase(repository, historyRepository)
    private val today = ItemStub.TODAY

    @Test
    fun `invoke leaves a non-recurring item untouched`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.None, dueDate = today.minusDays(10))

        val result = useCase(item, today)

        assertEquals(item, result.getOrNull())
        coVerify(exactly = 0) { historyRepository.insert(any()) }
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }

    @Test
    fun `invoke leaves a recurring item whose dueDate is not overdue untouched`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = today)

        val result = useCase(item, today)

        assertEquals(item, result.getOrNull())
        coVerify(exactly = 0) { historyRepository.insert(any()) }
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }

    @Test
    fun `invoke records one missed occurrence and advances dueDate past a single skipped cycle`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = today.minusWeeks(1))
        val missedRecord = ItemCompletionHistory(
            itemId = item.id,
            scheduledDate = today.minusWeeks(1),
            completedAt = null
        )
        val advanced = item.copy(dueDate = today)
        coEvery { historyRepository.insert(missedRecord) } returns 1L
        coEvery { repository.updateItem(advanced) } returns Unit

        val result = useCase(item, today)

        assertEquals(advanced, result.getOrNull())
        coVerify(exactly = 1) { historyRepository.insert(missedRecord) }
        coVerify(exactly = 1) { repository.updateItem(advanced) }
    }

    @Test
    fun `invoke records every missed occurrence when several cycles were skipped`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = today.minusWeeks(3))
        val advanced = item.copy(dueDate = today)
        coEvery { historyRepository.insert(any()) } returns 1L
        coEvery { repository.updateItem(advanced) } returns Unit

        val result = useCase(item, today)

        assertEquals(advanced, result.getOrNull())
        coVerify(exactly = 1) {
            historyRepository.insert(
                ItemCompletionHistory(itemId = item.id, scheduledDate = today.minusWeeks(3), completedAt = null),
            )
        }
        coVerify(exactly = 1) {
            historyRepository.insert(
                ItemCompletionHistory(itemId = item.id, scheduledDate = today.minusWeeks(2), completedAt = null),
            )
        }
        coVerify(exactly = 1) {
            historyRepository.insert(
                ItemCompletionHistory(itemId = item.id, scheduledDate = today.minusWeeks(1), completedAt = null),
            )
        }
        coVerify(exactly = 1) { repository.updateItem(advanced) }
    }

    @Test
    fun `invoke resets remindersMuted when advancing past a skipped cycle`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = today.minusWeeks(1), remindersMuted = true)
        val advanced = item.copy(dueDate = today, remindersMuted = false)
        coEvery { historyRepository.insert(any()) } returns 1L
        coEvery { repository.updateItem(advanced) } returns Unit

        val result = useCase(item, today)

        assertEquals(false, result.getOrNull()?.remindersMuted)
        coVerify(exactly = 1) { repository.updateItem(advanced) }
    }

    @Test
    fun `invoke skips MISSED for a cycle already completed and clears lastCompletedScheduledDate`() = runTest {
        val item = ItemStub.task(
            recurrence = Recurrence.Weekly,
            dueDate = today.minusWeeks(1),
            lastCompletedScheduledDate = today.minusWeeks(1),
        )
        val advanced = item.copy(dueDate = today, lastCompletedScheduledDate = null)
        coEvery { repository.updateItem(advanced) } returns Unit

        val result = useCase(item, today)

        assertEquals(advanced, result.getOrNull())
        coVerify(exactly = 0) { historyRepository.insert(any()) }
        coVerify(exactly = 1) { repository.updateItem(advanced) }
    }

    @Test
    fun `invoke carries pending extension fields onto the first MISSED entry and clears them from the item`() =
        runTest {
            val item = ItemStub.task(
                recurrence = Recurrence.Weekly,
                dueDate = today.minusWeeks(1),
                pendingExtensionOriginalDueDate = today.minusWeeks(3),
                pendingExtensionCount = 2,
            )
            val missedRecord = ItemCompletionHistory(
                itemId = item.id,
                scheduledDate = today.minusWeeks(1),
                completedAt = null,
                originalScheduledDate = today.minusWeeks(3),
                extensionCount = 2,
            )
            val advanced = item.copy(
                dueDate = today,
                pendingExtensionOriginalDueDate = null,
                pendingExtensionCount = 0,
            )
            coEvery { historyRepository.insert(missedRecord) } returns 1L
            coEvery { repository.updateItem(advanced) } returns Unit

            val result = useCase(item, today)

            assertEquals(advanced, result.getOrNull())
            coVerify(exactly = 1) { historyRepository.insert(missedRecord) }
            coVerify(exactly = 1) { repository.updateItem(advanced) }
        }
}
