package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.core.common.extensions.toLocalDate
import com.seucaio.unideas.core.common.extensions.toLocalDateTime
import com.seucaio.unideas.core.common.extensions.toLocalTime
import com.seucaio.unideas.core.common.extensions.toSecondOfDayInt
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.data.local.relation.ItemWithTags
import com.seucaio.unideas.data.local.relation.ItemWithTagsAndSection
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemDetail

private fun toItem(entity: ItemEntity, tags: List<TagEntity>): Item = Item(
    id = entity.id,
    type = entity.type,
    title = entity.title,
    description = entity.description,
    sectionId = entity.sectionId,
    dueDate = entity.dueDate?.toLocalDate(),
    dueTime = entity.dueTime?.toLocalTime(),
    recurrence = entity.recurrence,
    reminderWarning = entity.reminderWarning,
    completedAt = entity.completedAt?.toLocalDateTime(),
    createdAt = entity.createdAt.toLocalDateTime(),
    lastCompletedScheduledDate = entity.lastCompletedScheduledDate?.toLocalDate(),
    isPinned = entity.isPinned,
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
    dueTime = dueTime?.toSecondOfDayInt(),
    recurrence = recurrence,
    reminderWarning = reminderWarning,
    completedAt = completedAt?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
    lastCompletedScheduledDate = lastCompletedScheduledDate?.toEpochMilli(),
    isPinned = isPinned,
)
