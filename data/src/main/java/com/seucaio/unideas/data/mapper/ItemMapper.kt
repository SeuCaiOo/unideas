package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.core.common.extensions.toLocalDate
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.data.local.relation.ItemWithTags
import com.seucaio.unideas.data.local.relation.ItemWithTagsAndSection
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemDetail
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Converts epoch millis (as persisted in the database) to a [LocalDateTime]
 * using the system default time zone — same pattern as the `LocalDate`
 * extensions in `:core:common`.
 */
internal fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

/**
 * Converts a [LocalDateTime] to epoch millis in the system default time zone
 * (for writing to the database).
 */
internal fun LocalDateTime.toEpochMilli(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun toItem(entity: ItemEntity, tags: List<TagEntity>): Item = Item(
    id = entity.id,
    type = entity.type,
    title = entity.title,
    description = entity.description,
    sectionId = entity.sectionId,
    dueDate = entity.dueDate?.toLocalDate(),
    recurrence = entity.recurrence,
    completedAt = entity.completedAt?.toLocalDateTime(),
    createdAt = entity.createdAt.toLocalDateTime(),
    tags = tags.map { it.toDomain() },
)

internal fun ItemWithTags.toDomain(): Item = toItem(item, tags)

internal fun ItemWithTagsAndSection.toDomain(): ItemDetail = ItemDetail(
    item = toItem(item, tags),
    sectionName = section?.name,
)

internal fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    type = type,
    title = title,
    description = description,
    sectionId = sectionId,
    dueDate = dueDate?.toEpochMilli(),
    recurrence = recurrence,
    completedAt = completedAt?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
)
