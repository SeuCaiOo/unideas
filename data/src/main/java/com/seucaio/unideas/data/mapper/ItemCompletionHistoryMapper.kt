package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.core.common.extensions.toLocalDate
import com.seucaio.unideas.core.common.extensions.toLocalDateTime
import com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity
import com.seucaio.unideas.domain.model.ItemCompletionHistory

internal fun ItemCompletionHistoryEntity.toDomain(): ItemCompletionHistory = ItemCompletionHistory(
    id = id,
    itemId = itemId,
    scheduledDate = scheduledDate.toLocalDate(),
    completedAt = completedAt?.toLocalDateTime(),
    note = note,
    originalScheduledDate = originalScheduledDate?.toLocalDate(),
    extensionCount = extensionCount,
)

internal fun ItemCompletionHistory.toEntity(): ItemCompletionHistoryEntity =
    ItemCompletionHistoryEntity(
        id = id,
        itemId = itemId,
        scheduledDate = scheduledDate.toEpochMilli(),
        completedAt = completedAt?.toEpochMilli(),
        note = note,
        originalScheduledDate = originalScheduledDate?.toEpochMilli(),
        extensionCount = extensionCount,
    )
