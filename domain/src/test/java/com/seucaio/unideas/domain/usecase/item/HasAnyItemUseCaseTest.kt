package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HasAnyItemUseCaseTest {

    private val repository: ItemRepository = mockk()
    private val useCase = HasAnyItemUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        every { repository.hasAnyItem() } returns flowOf(true)

        val result = useCase().first()

        assertEquals(true, result)
        verify(exactly = 1) { repository.hasAnyItem() }
    }
}
