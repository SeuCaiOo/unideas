package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IgnoreOccurrenceUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val historyRepository: ItemCompletionHistoryRepository = mockk()
    private val reminderRefreshTrigger: ReminderRefreshTrigger = mockk()
    private val useCase = IgnoreOccurrenceUseCase(repository, historyRepository, reminderRefreshTrigger)
    private val note = "Não pude ler esses dias"

    init {
        every { reminderRefreshTrigger.refreshNow() } returns Unit
    }

    @Test
    fun `invoke records a missed occurrence with the note and advances dueDate one cycle`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
        val historyRecord = ItemCompletionHistory(
            itemId = item.id,
            scheduledDate = ItemStub.TODAY,
            completedAt = null,
            note = note,
        )
        val advanced = item.copy(dueDate = ItemStub.TODAY.plusWeeks(1))
        coEvery { historyRepository.insert(historyRecord) } returns 1L
        coEvery { repository.updateItem(advanced) } returns Unit

        val result = useCase(item, note, ItemStub.TODAY)

        assertEquals(advanced, result.getOrNull())
        coVerify(exactly = 1) { historyRepository.insert(historyRecord) }
        coVerify(exactly = 1) { repository.updateItem(advanced) }
    }

    @Test
    fun `invoke fails for a non-recurring item`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.None, dueDate = ItemStub.TODAY)

        val result = useCase(item, note, ItemStub.TODAY)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { historyRepository.insert(any()) }
    }

    @Test
    fun `invoke fails when dueDate is in the future`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY.plusDays(1))

        val result = useCase(item, note, ItemStub.TODAY)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { historyRepository.insert(any()) }
    }

    @Test
    fun `invoke fails with a blank note`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)

        val result = useCase(item, "   ", ItemStub.TODAY)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { historyRepository.insert(any()) }
    }
}
