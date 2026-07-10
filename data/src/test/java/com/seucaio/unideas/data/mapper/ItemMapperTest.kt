package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.data.local.relation.ItemWithTags
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.TagStub
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

class ItemMapperTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        // Pin a non-UTC zone (UTC-3, no DST) so the system-default conversions
        // are deterministic regardless of the machine running the tests.
        TimeZone.setDefault(TimeZone.getTimeZone(SAO_PAULO))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `toEntity maps every field converting dates to epoch millis`() {
        val item = ItemStub.task(
            id = 10L,
            description = "detalhes",
            sectionId = 3L,
            recurrence = Recurrence.WEEKLY,
            completedAt = ItemStub.TODAY.atTime(9, 30),
        )

        val entity = item.toEntity()

        assertEquals(10L, entity.id)
        assertEquals(ItemType.TASK, entity.type)
        assertEquals(item.title, entity.title)
        assertEquals("detalhes", entity.description)
        assertEquals(3L, entity.sectionId)
        assertEquals(item.dueDate?.toEpochMilli(), entity.dueDate)
        assertEquals(Recurrence.WEEKLY, entity.recurrence)
        assertEquals(item.completedAt?.toEpochMilli(), entity.completedAt)
        assertEquals(item.createdAt.toEpochMilli(), entity.createdAt)
    }

    @Test
    fun `toEntity keeps optional fields null`() {
        val entity = ItemStub.note().toEntity()

        assertNull(entity.sectionId)
        assertNull(entity.dueDate)
        assertNull(entity.completedAt)
        assertEquals(Recurrence.NONE, entity.recurrence)
    }

    @Test
    fun `toDomain maps joined tags to domain tags`() {
        val entities = listOf(TagEntity(id = 1L, name = "tag-1"), TagEntity(id = 2L, name = "tag-2"))
        val row = ItemWithTags(item = ItemStub.task().toEntity(), tags = entities)

        val domain = row.toDomain()

        assertEquals(TagStub.tags(count = 2), domain.tags)
    }

    @Test
    fun `toEntity and toDomain round-trip preserves the item`() {
        val original = ItemStub.task(
            id = 7L,
            description = "detalhes",
            sectionId = 2L,
            recurrence = Recurrence.MONTHLY,
            completedAt = ItemStub.TODAY.atTime(18, 45),
            tags = TagStub.tags(count = 2),
        )

        val row = ItemWithTags(
            item = original.toEntity(),
            tags = original.tags.map { TagEntity(id = it.id, name = it.name) },
        )

        assertEquals(original, row.toDomain())
    }

    @Test
    fun `round-trip preserves nulls for a minimal note`() {
        val original = ItemStub.note()

        val row = ItemWithTags(item = original.toEntity(), tags = emptyList())

        assertEquals(original, row.toDomain())
    }

    @Test
    fun `LocalDateTime toEpochMilli uses system default zone`() {
        val dateTime = LocalDateTime.of(2026, 7, 9, 10, 30)

        val expected = dateTime.atZone(ZoneId.of(SAO_PAULO)).toInstant().toEpochMilli()

        assertEquals(expected, dateTime.toEpochMilli())
    }

    @Test
    fun `LocalDateTime toEpochMilli and toLocalDateTime round-trip`() {
        val dateTime = LocalDateTime.of(2026, 2, 28, 23, 59, 59)

        assertEquals(dateTime, dateTime.toEpochMilli().toLocalDateTime())
    }

    private companion object {
        const val SAO_PAULO = "America/Sao_Paulo"
    }
}
