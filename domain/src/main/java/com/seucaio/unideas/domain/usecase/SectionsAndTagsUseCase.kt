package com.seucaio.unideas.domain.usecase

import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

class SectionsAndTagsUseCase(
    private val sectionUseCase: SectionUseCase,
    private val tagUseCase: TagUseCase,
) {
    fun getAll(): Flow<SectionsAndTags> = combine(
        sectionUseCase.getAll().catch { emit(emptyList()) },
        tagUseCase.getAll().catch { emit(emptyList()) },
    ) { sections, tags -> SectionsAndTags(sections, tags) }

    suspend fun addSection(name: String): Result<Long> = sectionUseCase.add(name)

    suspend fun addTag(name: String): Result<Long> = tagUseCase.add(name)
}
