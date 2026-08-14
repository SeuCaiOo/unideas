package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ItemCompletionHistoryMapperTest {

    @Test
    fun `toEntity maps every field converting dates to epoch millis`() {
        val history = ItemCompletionHistory(
            id = 5L,
            itemId = 10L,
            scheduledDate = LocalDate.of(2026, 7, 1),
            completedAt = LocalDateTime.of(2026, 7, 2, 9, 30),
            note = "atrasei",
        )

        val entity = history.toEntity()

        assertEquals(5L, entity.id)
        assertEquals(10L, entity.itemId)
        assertEquals(history.scheduledDate.toEpochMilli(), entity.scheduledDate)
        assertEquals(history.completedAt?.toEpochMilli(), entity.completedAt)
        assertEquals("atrasei", entity.note)
    }

    @Test
    fun `toEntity keeps completedAt null when missed`() {
        val history = ItemCompletionHistory(itemId = 10L, scheduledDate = LocalDate.of(2026, 7, 1), completedAt = null)

        val entity = history.toEntity()

        assertNull(entity.completedAt)
    }

    @Test
    fun `toEntity and toDomain round-trip preserves the record`() {
        val original = ItemCompletionHistory(
            id = 7L,
            itemId = 3L,
            scheduledDate = LocalDate.of(2026, 6, 15),
            completedAt = LocalDateTime.of(2026, 6, 16, 8, 0),
            note = "corrigido manualmente",
        )

        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun `toEntity and toDomain round-trip preserves extension tracking fields`() {
        val original = ItemCompletionHistory(
            id = 8L,
            itemId = 3L,
            scheduledDate = LocalDate.of(2026, 6, 22),
            completedAt = LocalDateTime.of(2026, 6, 23, 8, 0),
            originalScheduledDate = LocalDate.of(2026, 6, 15),
            extensionCount = 2,
        )

        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun `toDomain maps a missed occurrence entity`() {
        val entity = ItemCompletionHistoryEntity(
            id = 1L,
            itemId = 2L,
            scheduledDate = LocalDate.of(2026, 7, 1).toEpochMilli(),
            completedAt = null,
        )

        val domain = entity.toDomain()

        assertNull(domain.completedAt)
        assertEquals(2L, domain.itemId)
    }
}
