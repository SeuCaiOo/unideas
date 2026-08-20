package com.seucaio.unideas.data.repository

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.data.local.dao.ItemCompletionHistoryDao
import com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import io.mockk.coEvery
import io.mockk.coVerify
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

class ItemCompletionHistoryRepositoryImplTest {

    private val dao: ItemCompletionHistoryDao = mockk()
    private val repository = ItemCompletionHistoryRepositoryImpl(dao)

    @Test
    fun `getHistory delegates to the dao and maps rows to domain`() = runTest {
        val entity = ItemCompletionHistoryEntity(
            id = 1L,
            itemId = 10L,
            scheduledDate = LocalDate.of(2026, 7, 1).toEpochMilli(),
            completedAt = null,
        )
        every { dao.getHistory(10L) } returns flowOf(listOf(entity))

        val result = repository.getHistory(10L).first()

        assertEquals(1, result.size)
        assertEquals(10L, result.first().itemId)
        verify(exactly = 1) { dao.getHistory(10L) }
    }

    @Test
    fun `insert delegates the mapped entity returning the generated id`() = runTest {
        val record = ItemCompletionHistory(
            itemId = 10L,
            scheduledDate = LocalDate.of(2026, 7, 1),
            completedAt = LocalDateTime.of(2026, 7, 1, 9, 0),
        )
        coEvery { dao.insert(record.toEntity()) } returns 42L

        val id = repository.insert(record)

        assertEquals(42L, id)
        coVerify(exactly = 1) { dao.insert(record.toEntity()) }
    }

    @Test
    fun `update delegates the mapped entity to the dao`() = runTest {
        val record = ItemCompletionHistory(
            id = 5L,
            itemId = 10L,
            scheduledDate = LocalDate.of(2026, 7, 1),
            completedAt = LocalDateTime.of(2026, 7, 1, 9, 0),
        )
        coEvery { dao.update(record.toEntity()) } returns Unit

        repository.update(record)

        coVerify(exactly = 1) { dao.update(record.toEntity()) }
    }

    @Test
    fun `deleteById delegates to the dao`() = runTest {
        coEvery { dao.deleteById(5L) } returns Unit

        repository.deleteById(5L)

        coVerify(exactly = 1) { dao.deleteById(5L) }
    }
}
