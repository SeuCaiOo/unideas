package com.seucaio.unideas.domain.usecase

import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

/**
 * Snapshots the sections and tags available for selection in one call — a one-time read, not
 * a live [SectionsAndTags] flow, since nothing on the calling screens can create a section/tag
 * without leaving them first.
 */
class GetSectionsAndTagsUseCase(
    private val getSections: GetSectionsUseCase,
    private val getTags: GetTagsUseCase,
) {
    suspend operator fun invoke(): SectionsAndTags = SectionsAndTags(
        sections = getSections().catch { emit(emptyList()) }.first(),
        tags = getTags().catch { emit(emptyList()) }.first(),
    )
}
