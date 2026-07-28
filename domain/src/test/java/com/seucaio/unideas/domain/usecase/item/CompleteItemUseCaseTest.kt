package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val historyRepository: ItemCompletionHistoryRepository = mockk()
    private val reminderRefreshTrigger: ReminderRefreshTrigger = mockk()
    private val useCase = CompleteItemUseCase(repository, historyRepository, reminderRefreshTrigger)
    private val completedAt = ItemStub.TODAY.atTime(12, 0)

    init {
        every { reminderRefreshTrigger.refreshNow() } returns Unit
    }

    @Test
    fun `invoke completes a non-recurring task without touching history`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.None)
        coEvery { repository.updateItem(item.copy(completedAt = completedAt)) } returns Unit

        val result = useCase(item, completedAt)

        assertEquals(CompletionResult.Completed, result.getOrNull())
        coVerify(exactly = 0) { historyRepository.insert(any()) }
        verify(exactly = 1) { reminderRefreshTrigger.refreshNow() }
    }

    @Test
    fun `invoke completes a recurring task by recording history and advancing dueDate on the same row`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
        val historyRecord =
            ItemCompletionHistory(itemId = item.id, scheduledDate = ItemStub.TODAY, completedAt = completedAt)
        val advanced = item.copy(dueDate = ItemStub.TODAY.plusWeeks(1))
        coEvery { historyRepository.insert(historyRecord) } returns 1L
        coEvery { repository.updateItem(advanced) } returns Unit

        val result = useCase(item, completedAt)

        assertEquals(CompletionResult.CompletedAndRenewed(item.id), result.getOrNull())
        coVerify(exactly = 1) { historyRepository.insert(historyRecord) }
        coVerify(exactly = 1) { repository.updateItem(advanced) }
        coVerify(exactly = 0) { repository.insertItem(any()) }
    }

    @Test
    fun `invoke uncompletes an already-completed task without touching recurrence`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY, completedAt = completedAt)
        coEvery { repository.updateItem(item.copy(completedAt = null)) } returns Unit

        val result = useCase(item, completedAt)

        assertEquals(CompletionResult.Uncompleted, result.getOrNull())
        coVerify(exactly = 0) { historyRepository.insert(any()) }
    }

    @Test
    fun `invoke fails for a NOTE and does not touch the repository`() = runTest {
        val note = ItemStub.note()

        val result = useCase(note, completedAt)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
        coVerify(exactly = 0) { historyRepository.insert(any()) }
        verify(exactly = 0) { reminderRefreshTrigger.refreshNow() }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.None)
        coEvery { repository.updateItem(item.copy(completedAt = completedAt)) } throws IllegalStateException("boom")

        val result = useCase(item, completedAt)

        assertTrue(result.isFailure)
        verify(exactly = 0) { reminderRefreshTrigger.refreshNow() }
    }
}
