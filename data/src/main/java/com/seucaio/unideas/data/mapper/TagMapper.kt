package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.domain.model.Tag

internal fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
)

internal fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
)
