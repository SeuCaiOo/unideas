package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class EditItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = EditItemUseCase(repository)
    private val now = ItemStub.TODAY.atTime(20, 0)

    @Test
    fun `invoke updates the item stamping updatedAt with now`() = runTest {
        val item = ItemStub.task(id = 7L)
        val expected = item.copy(updatedAt = now)
        coEvery { repository.updateItem(expected) } returns Unit

        val result = useCase(item, now)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateItem(expected) }
    }

    @Test
    fun `invoke fails when title is blank and does not call the repository`() = runTest {
        val item = ItemStub.task(title = " ")

        val result = useCase(item, now)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val item = ItemStub.task(id = 7L)
        coEvery { repository.updateItem(any()) } throws IllegalStateException("boom")

        val result = useCase(item, now)

        assertTrue(result.isFailure)
    }
}
