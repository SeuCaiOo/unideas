package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.model.ItemStatus
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
     * Observes items of a Home tab ([type]), optionally filtered by section and/or tags —
     * excludes [ItemStatus.ARCHIVED] items (see [getArchivedItems]).
     *
     * @param sectionId `null` = no section filter.
     * @param tagIds empty = no tag filter; otherwise items linked to any of the ids.
     */
    fun getItems(type: ItemType, sectionId: Long?, tagIds: List<Long>): Flow<List<Item>>

    /** Observes a single item, `null` when it does not exist (e.g. after deletion). */
    fun getItem(id: Long): Flow<Item?>

    /**
     * Observes a single item together with its resolved [ItemDetail.sectionName], `null` when
     * the item does not exist. The join happens in `:data` (Room relation), never in memory.
     */
    fun getItemDetail(id: Long): Flow<ItemDetail?>

    /**
     * Observes non-completed, non-archived items due on or before [dueOnOrBefore] (overdue +
     * due soon), ordered by due date. The caller computes the threshold date
     * (today + N) and applies any panel limit.
     */
    fun getPriorityItems(dueOnOrBefore: LocalDate): Flow<List<Item>>

    /**
     * Observes every non-completed, non-archived item with a due date, regardless of how far
     * out — unlike [getPriorityItems], not bounded by the "due soon" window. Used by the
     * reminder check worker (#115), since a configured warning can be further out than that
     * window; excluding archived items here is what pauses their reminders/occurrence
     * processing (#168).
     */
    fun getItemsWithDueDate(): Flow<List<Item>>

    /** Observes whether the item table has any row at all, regardless of type/section/tags. */
    fun hasAnyItem(): Flow<Boolean>

    /** Observes every archived item ([ItemStatus.ARCHIVED]), most recently created first. */
    fun getArchivedItems(): Flow<List<Item>>

    /** Inserts [item] (and its tag links) and returns the generated id. */
    suspend fun insertItem(item: Item): Long

    /** Updates [item] (and its tag links) by [Item.id]. */
    suspend fun updateItem(item: Item)

    /** Deletes the item with [id]; tag links go with it. */
    suspend fun deleteItem(id: Long)

    /** Sets [Item.isPinned] for the item with [id] — pinned items appear in the priority panel regardless of urgency. */
    suspend fun setItemPinned(id: Long, isPinned: Boolean)

    /** Sets [Item.status] for the item with [id]. */
    suspend fun setItemStatus(id: Long, status: ItemStatus)
}
