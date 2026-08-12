package com.seucaio.unideas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.relation.ItemWithTags
import com.seucaio.unideas.data.local.relation.ItemWithTagsAndSection
import com.seucaio.unideas.domain.model.ItemType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for items and their tag links. Reads return [Flow] with tags already
 * joined via `@Relation`; writes that touch `item_tag` run in a transaction.
 */
@Dao
interface ItemDao {

    /**
     * Observes items of [type], optionally filtered by section and/or tags.
     *
     * @param sectionId `null` = no section filter.
     * @param tagCount pass `tagIds.size`; `0` disables the tag filter
     *   (an empty `IN ()` never matches, so the size is bound explicitly).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM items
        WHERE type = :type
          AND (:sectionId IS NULL OR sectionId = :sectionId)
          AND (:tagCount = 0 OR id IN (SELECT itemId FROM item_tag WHERE tagId IN (:tagIds)))
        ORDER BY createdAt DESC
        """,
    )
    fun getItems(
        type: ItemType,
        sectionId: Long?,
        tagIds: List<Long>,
        tagCount: Int,
    ): Flow<List<ItemWithTags>>

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Long): Flow<ItemWithTags?>

    /** Same row as [getItemById], with the section joined too — backs the detail screen. */
    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemDetailById(id: Long): Flow<ItemWithTagsAndSection?>

    /**
     * Observes non-completed items due on or before [dueOnOrBefore] (epoch millis), plus every
     * pinned item regardless of due date — pinned items always surface in the priority panel.
     * Ordered pinned-first, then by due date (items without a due date sort last within each group).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM items
        WHERE completedAt IS NULL
          AND (isPinned = 1 OR (dueDate IS NOT NULL AND dueDate <= :dueOnOrBefore))
        ORDER BY isPinned DESC, dueDate IS NULL, dueDate ASC
        """,
    )
    fun getPriorityItems(dueOnOrBefore: Long): Flow<List<ItemWithTags>>

    /**
     * Observes every non-completed item with a due date, regardless of how far out — unlike
     * [getPriorityItems], not bounded by the "due soon" window. Used by the reminder check
     * worker (#115), since a configured warning can be further out than that window.
     */
    @Transaction
    @Query("SELECT * FROM items WHERE dueDate IS NOT NULL AND completedAt IS NULL")
    fun getItemsWithDueDate(): Flow<List<ItemWithTags>>

    /** Cheap existence check — no items in the entire table, regardless of type/section/tags. */
    @Query("SELECT EXISTS(SELECT 1 FROM items)")
    fun hasAnyItem(): Flow<Boolean>

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE items SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(refs: List<ItemTagCrossRef>)

    @Query("DELETE FROM item_tag WHERE itemId = :itemId")
    suspend fun deleteTagCrossRefsByItemId(itemId: Long)

    /** Inserts [item] and links it to [tagIds] atomically; returns the generated id. */
    @Transaction
    suspend fun insertItemWithTags(item: ItemEntity, tagIds: List<Long>): Long {
        val id = insert(item)
        if (tagIds.isNotEmpty()) {
            insertTagCrossRefs(tagIds.map { ItemTagCrossRef(itemId = id, tagId = it) })
        }
        return id
    }

    /** Updates [item] and replaces its tag links with [tagIds] atomically. */
    @Transaction
    suspend fun updateItemWithTags(item: ItemEntity, tagIds: List<Long>) {
        update(item)
        deleteTagCrossRefsByItemId(item.id)
        if (tagIds.isNotEmpty()) {
            insertTagCrossRefs(tagIds.map { ItemTagCrossRef(itemId = item.id, tagId = it) })
        }
    }
}
