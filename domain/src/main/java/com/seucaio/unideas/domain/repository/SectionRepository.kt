package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.Section
import kotlinx.coroutines.flow.Flow

/**
 * Contract for section persistence. Implemented in `:data` (Room) and injected via DI.
 */
interface SectionRepository {

    /** Observes all sections. */
    fun getSections(): Flow<List<Section>>

    /** Observes sections that have at least one item tagged with any of [tagIds]. */
    fun getSectionsByTags(tagIds: List<Long>): Flow<List<Section>>

    /** Inserts [section] and returns the generated id. */
    suspend fun insertSection(section: Section): Long

    /** Updates [section] (rename) by [Section.id]. */
    suspend fun updateSection(section: Section)

    /**
     * Deletes the section with [id]. Callers must check [countLinkedItems]
     * first — deletion with linked items is blocked at the use-case level.
     */
    suspend fun deleteSection(id: Long)

    /** Number of items currently linked to the section with [sectionId]. */
    suspend fun countLinkedItems(sectionId: Long): Int
}
