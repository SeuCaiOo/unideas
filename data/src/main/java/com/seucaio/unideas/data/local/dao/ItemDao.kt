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

    /**
     * Observes non-completed items with a due date on or before
     * [dueOnOrBefore] (epoch millis), ordered by due date.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM items
        WHERE dueDate IS NOT NULL
          AND dueDate <= :dueOnOrBefore
          AND completedAt IS NULL
        ORDER BY dueDate ASC
        """,
    )
    fun getPriorityItems(dueOnOrBefore: Long): Flow<List<ItemWithTags>>

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: Long)

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
