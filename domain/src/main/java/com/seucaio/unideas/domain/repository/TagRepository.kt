package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Contract for tag persistence. Implemented in `:data` (Room) and injected via DI.
 */
interface TagRepository {

    /** Observes all tags. */
    fun getTags(): Flow<List<Tag>>

    /** Inserts [tag] and returns the generated id. */
    suspend fun insertTag(tag: Tag): Long

    /** Updates [tag] (rename) by [Tag.id]. */
    suspend fun updateTag(tag: Tag)

    /**
     * Deletes the tag with [id]. Callers must check [countLinkedItems] first —
     * deletion with linked items is blocked at the use-case level.
     */
    suspend fun deleteTag(id: Long)

    /** Number of items currently linked to the tag with [tagId]. */
    suspend fun countLinkedItems(tagId: Long): Int
}
