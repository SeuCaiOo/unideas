package com.seucaio.unideas.domain.usecase

import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

/**
 * Snapshots the sections and tags available for selection in one call — a one-time read, not
 * a live [SectionsAndTags] flow, since nothing on the calling screens can create a section/tag
 * without leaving them first. [SectionUseCase.getAll]/[TagUseCase.getAll] already log via
 * [logOnError] upstream — degrading to an empty list here is a deliberate, already-traced choice,
 * not a second silent swallow.
 */
class GetSectionsAndTagsUseCase(
    private val sectionUseCase: SectionUseCase,
    private val tagUseCase: TagUseCase,
) {
    suspend operator fun invoke(): SectionsAndTags = SectionsAndTags(
        sections = sectionUseCase.getAll().catch { emit(emptyList()) }.first(),
        tags = tagUseCase.getAll().catch { emit(emptyList()) }.first(),
    )
}
