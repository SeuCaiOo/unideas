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

    @Test
    fun `invoke updates the item`() = runTest {
        val item = ItemStub.task(id = 7L)
        coEvery { repository.updateItem(item) } returns Unit

        val result = useCase(item)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateItem(item) }
    }

    @Test
    fun `invoke fails when title is blank and does not call the repository`() = runTest {
        val item = ItemStub.task(title = " ")

        val result = useCase(item)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateItem(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val item = ItemStub.task(id = 7L)
        coEvery { repository.updateItem(item) } throws IllegalStateException("boom")

        val result = useCase(item)

        assertTrue(result.isFailure)
    }
}
