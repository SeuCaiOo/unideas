package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contract for item persistence. Implemented in `:data` (Room) and injected via DI.
 *
 * Observable reads return [Flow]; writes are `suspend`. Items come with their
 * [Item.tags] already joined (relation resolved in the data layer, never in memory).
 */
interface ItemRepository {

    /**
     * Observes items of a Home tab ([type]), optionally filtered by section
     * and/or tags.
     *
     * @param sectionId `null` = no section filter.
     * @param tagIds empty = no tag filter; otherwise items linked to any of the ids.
     */
    fun getItems(type: ItemType, sectionId: Long?, tagIds: List<Long>): Flow<List<Item>>

    /** Observes a single item, `null` when it does not exist (e.g. after deletion). */
    fun getItemById(id: Long): Flow<Item?>

    /**
     * Observes non-completed items due on or before [dueOnOrBefore] (overdue +
     * due soon), ordered by due date. The caller computes the threshold date
     * (today + N) and applies any panel limit.
     */
    fun getPriorityItems(dueOnOrBefore: LocalDate): Flow<List<Item>>

    /** Inserts [item] (and its tag links) and returns the generated id. */
    suspend fun insertItem(item: Item): Long

    /** Updates [item] (and its tag links) by [Item.id]. */
    suspend fun updateItem(item: Item)

    /** Deletes the item with [id]; tag links go with it. */
    suspend fun deleteItem(id: Long)
}
