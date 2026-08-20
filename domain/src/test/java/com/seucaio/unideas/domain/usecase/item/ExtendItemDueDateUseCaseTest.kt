package com.seucaio.unideas.domain.usecase.item

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

class ExtendItemDueDateUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val reminderRefreshTrigger: ReminderRefreshTrigger = mockk()
    private val useCase = ExtendItemDueDateUseCase(repository, reminderRefreshTrigger)

    init {
        every { reminderRefreshTrigger.refreshNow() } returns Unit
    }

    @Test
    fun `invoke moves dueDate forward without writing to history`() = runTest {
        val originalDueDate = ItemStub.TODAY.minusDays(3)
        val item = ItemStub.task(dueDate = originalDueDate)
        val newDueDate = ItemStub.TODAY.plusDays(2)
        val extended = item.copy(
            dueDate = newDueDate,
            pendingExtensionOriginalDueDate = originalDueDate,
            pendingExtensionCount = 1,
        )
        coEvery { repository.updateItem(extended) } returns Unit

        val result = useCase(item, newDueDate, ItemStub.TODAY)

        assertEquals(extended, result.getOrNull())
        coVerify(exactly = 1) { repository.updateItem(extended) }
    }

    @Test
    fun `invoke keeps the first original dueDate and increments the count on a second extension`() =
        runTest {
            val originalDueDate = ItemStub.TODAY.minusDays(5)
            val item = ItemStub.task(
                dueDate = ItemStub.TODAY.minusDays(1),
                pendingExtensionOriginalDueDate = originalDueDate,
                pendingExtensionCount = 1,
            )
            val newDueDate = ItemStub.TODAY.plusDays(2)
            val extended = item.copy(
                dueDate = newDueDate,
                pendingExtensionOriginalDueDate = originalDueDate,
                pendingExtensionCount = 2,
            )
            coEvery { repository.updateItem(extended) } returns Unit

            val result = useCase(item, newDueDate, ItemStub.TODAY)

            assertEquals(extended, result.getOrNull())
            coVerify(exactly = 1) { repository.updateItem(extended) }
        }

    @Test
    fun `invoke fails when the item is not overdue`() = runTest {
        val item = ItemStub.task(dueDate = ItemStub.TODAY.plusDays(1))

        val result = useCase(item, ItemStub.TODAY.plusDays(5), ItemStub.TODAY)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }

    @Test
    fun `invoke fails when newDueDate is not after the current dueDate`() = runTest {
        val item = ItemStub.task(dueDate = ItemStub.TODAY.minusDays(3))

        val result = useCase(item, ItemStub.TODAY.minusDays(3), ItemStub.TODAY)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }

    @Test
    fun `invoke fails when the item has no dueDate`() = runTest {
        val item = ItemStub.task(dueDate = null)

        val result = useCase(item, ItemStub.TODAY.plusDays(1), ItemStub.TODAY)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }
}
