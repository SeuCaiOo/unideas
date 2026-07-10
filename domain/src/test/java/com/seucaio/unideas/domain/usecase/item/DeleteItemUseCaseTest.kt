package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = DeleteItemUseCase(repository)

    @Test
    fun `invoke delegates the exact id to the repository`() = runTest {
        coEvery { repository.deleteItem(7L) } returns Unit

        useCase(7L)

        coVerify(exactly = 1) { repository.deleteItem(7L) }
    }
}
