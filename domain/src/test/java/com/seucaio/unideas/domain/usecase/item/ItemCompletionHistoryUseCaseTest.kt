package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ItemCompletionHistoryUseCaseTest {

    private val repository: ItemCompletionHistoryRepository = mockk()
    private val useCase = ItemCompletionHistoryUseCase(repository)

    @Test
    fun `getHistory delegates to the repository`() = runTest {
        val history = listOf(
            ItemCompletionHistory(itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null),
        )
        every { repository.getHistory(1L) } returns flowOf(history)

        val result = useCase.getHistory(1L).first()

        assertEquals(history, result)
        verify(exactly = 1) { repository.getHistory(1L) }
    }

    @Test
    fun `save with id 0 inserts a new entry`() = runTest {
        val record = ItemCompletionHistory(itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null)
        every { repository.getHistory(1L) } returns flowOf(emptyList())
        coEvery { repository.insert(record) } returns 10L

        val result = useCase.save(record)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.insert(record) }
        coVerify(exactly = 0) { repository.update(any()) }
    }

    @Test
    fun `save with a non-zero id updates the existing entry`() = runTest {
        val record = ItemCompletionHistory(id = 7L, itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null)
        every { repository.getHistory(1L) } returns flowOf(listOf(record))
        coEvery { repository.update(record) } returns Unit

        val result = useCase.save(record)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.update(record) }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `save fails when scheduledDate is in the future`() = runTest {
        val record = ItemCompletionHistory(
            itemId = 1L,
            scheduledDate = LocalDate.now().plusDays(1),
            completedAt = null,
        )
        every { repository.getHistory(1L) } returns flowOf(emptyList())

        val result = useCase.save(record)

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `save fails when late and note is blank`() = runTest {
        val record = ItemCompletionHistory(
            itemId = 1L,
            scheduledDate = ItemStub.TODAY.minusDays(1),
            completedAt = ItemStub.TODAY.atTime(9, 0),
            note = null,
        )
        every { repository.getHistory(1L) } returns flowOf(emptyList())

        val result = useCase.save(record)

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `save succeeds when late and note is provided`() = runTest {
        val record = ItemCompletionHistory(
            itemId = 1L,
            scheduledDate = ItemStub.TODAY.minusDays(1),
            completedAt = ItemStub.TODAY.atTime(9, 0),
            note = "Atrasei",
        )
        every { repository.getHistory(1L) } returns flowOf(emptyList())
        coEvery { repository.insert(record) } returns 10L

        val result = useCase.save(record)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.insert(record) }
    }

    @Test
    fun `save fails when another entry already exists for the same scheduledDate`() = runTest {
        val existing = ItemCompletionHistory(id = 2L, itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null)
        val record = ItemCompletionHistory(itemId = 1L, scheduledDate = ItemStub.TODAY, completedAt = null)
        every { repository.getHistory(1L) } returns flowOf(listOf(existing))

        val result = useCase.save(record)

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `delete calls the repository's deleteById`() = runTest {
        coEvery { repository.deleteById(5L) } returns Unit

        val result = useCase.delete(5L)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { repository.deleteById(5L) }
    }
}
