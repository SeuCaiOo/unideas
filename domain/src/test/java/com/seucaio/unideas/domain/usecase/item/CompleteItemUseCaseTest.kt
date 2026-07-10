package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = CompleteItemUseCase(repository)
    private val completedAt = ItemStub.TODAY.atTime(12, 0)

    @Test
    fun `invoke completes a non-recurring task without creating a new instance`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.None)
        coEvery { repository.updateItem(item.copy(completedAt = completedAt)) } returns Unit

        val result = useCase(item, completedAt)

        assertEquals(CompletionResult.Completed, result.getOrNull())
        coVerify(exactly = 0) { repository.insertItem(any()) }
    }

    @Test
    fun `invoke completes a recurring task and spawns a new instance from the original due date`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.Weekly, dueDate = ItemStub.TODAY)
        val renewed = item.copy(
            id = 0L,
            dueDate = item.dueDate!!.plusWeeks(1),
            completedAt = null,
            createdAt = completedAt,
        )
        coEvery { repository.updateItem(item.copy(completedAt = completedAt)) } returns Unit
        coEvery { repository.insertItem(renewed) } returns 99L

        val result = useCase(item, completedAt)

        assertEquals(CompletionResult.CompletedAndRenewed(99L), result.getOrNull())
        coVerify(exactly = 1) { repository.insertItem(renewed) }
    }

    @Test
    fun `invoke fails for a NOTE and does not touch the repository`() = runTest {
        val note = ItemStub.note()

        val result = useCase(note, completedAt)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
        coVerify(exactly = 0) { repository.insertItem(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val item = ItemStub.task(recurrence = Recurrence.None)
        coEvery { repository.updateItem(item.copy(completedAt = completedAt)) } throws IllegalStateException("boom")

        val result = useCase(item, completedAt)

        assertTrue(result.isFailure)
    }
}
