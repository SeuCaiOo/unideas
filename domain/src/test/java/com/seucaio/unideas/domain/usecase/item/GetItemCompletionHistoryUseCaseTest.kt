package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GetItemCompletionHistoryUseCaseTest {

    private val repository: ItemCompletionHistoryRepository = mockk()
    private val useCase = GetItemCompletionHistoryUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val history = listOf(
            ItemCompletionHistory(
                id = 1L,
                itemId = 10L,
                scheduledDate = LocalDate.of(2026, 7, 1),
                completedAt = LocalDateTime.of(2026, 7, 1, 9, 0),
            ),
        )
        every { repository.getHistory(10L) } returns flowOf(history)

        val result = useCase(10L).first()

        assertEquals(history, result)
        verify(exactly = 1) { repository.getHistory(10L) }
    }
}
