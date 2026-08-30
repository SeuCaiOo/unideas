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

class SetRemindersMutedUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val reminderRefreshTrigger: ReminderRefreshTrigger = mockk()
    private val useCase = SetRemindersMutedUseCase(repository, reminderRefreshTrigger)

    init {
        every { reminderRefreshTrigger.refreshNow() } returns Unit
    }

    @Test
    fun `invoke mutes reminders on an item with a dueDate`() = runTest {
        val item = ItemStub.task(dueDate = ItemStub.TODAY.plusDays(3), remindersMuted = false)
        val muted = item.copy(remindersMuted = true)
        coEvery { repository.updateItem(muted) } returns Unit

        val result = useCase(item, muted = true)

        assertEquals(muted, result.getOrNull())
        coVerify(exactly = 1) { repository.updateItem(muted) }
        coVerify(exactly = 1) { reminderRefreshTrigger.refreshNow() }
    }

    @Test
    fun `invoke unmutes reminders`() = runTest {
        val item = ItemStub.task(dueDate = ItemStub.TODAY.plusDays(3), remindersMuted = true)
        val unmuted = item.copy(remindersMuted = false)
        coEvery { repository.updateItem(unmuted) } returns Unit

        val result = useCase(item, muted = false)

        assertEquals(unmuted, result.getOrNull())
        coVerify(exactly = 1) { repository.updateItem(unmuted) }
    }

    @Test
    fun `invoke fails when the item has no dueDate`() = runTest {
        val item = ItemStub.task(dueDate = null)

        val result = useCase(item, muted = true)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }
}
