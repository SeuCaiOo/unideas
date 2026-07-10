package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.data.local.entity.SectionEntity
import com.seucaio.unideas.domain.model.Section

internal fun SectionEntity.toDomain(): Section = Section(
    id = id,
    name = name,
)

internal fun Section.toEntity(): SectionEntity = SectionEntity(
    id = id,
    name = name,
)
