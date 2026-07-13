package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.stub.ItemStub
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = CreateItemUseCase(repository)

    @Test
    fun `invoke inserts the item and returns the generated id`() = runTest {
        val item = ItemStub.task(id = 0L)
        coEvery { repository.insertItem(item) } returns 42L

        val result = useCase(item)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == 42L)
        coVerify(exactly = 1) { repository.insertItem(item) }
    }

    @Test
    fun `invoke fails when title is blank and does not call the repository`() = runTest {
        val item = ItemStub.task(title = " ")

        val result = useCase(item)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.insertItem(any()) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        val item = ItemStub.task(id = 0L)
        coEvery { repository.insertItem(item) } throws IllegalStateException("boom")

        val result = useCase(item)

        assertTrue(result.isFailure)
    }
}
