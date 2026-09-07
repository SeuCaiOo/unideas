package com.seucaio.unideas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.seucaio.unideas.data.local.entity.ItemLinkEntity
import com.seucaio.unideas.data.local.relation.ItemWithTags
import kotlinx.coroutines.flow.Flow

/** DAO for the symmetric item↔item link (`item_link` table). */
@Dao
interface ItemLinkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinkEntity(link: ItemLinkEntity)

    @Query("DELETE FROM item_link WHERE itemIdA = :itemIdA AND itemIdB = :itemIdB")
    suspend fun deleteLinkEntity(itemIdA: Long, itemIdB: Long)

    @Transaction
    @Query(
        """
        SELECT * FROM items WHERE id IN (
            SELECT itemIdB FROM item_link WHERE itemIdA = :itemId
            UNION
            SELECT itemIdA FROM item_link WHERE itemIdB = :itemId
        )
        """,
    )
    fun getLinkedItems(itemId: Long): Flow<List<ItemWithTags>>

    /** Normalizes the pair before inserting so `A→B` and `B→A` are never stored as two rows. */
    suspend fun insertLink(itemIdA: Long, itemIdB: Long) {
        val (a, b) = normalize(itemIdA, itemIdB)
        insertLinkEntity(ItemLinkEntity(itemIdA = a, itemIdB = b))
    }

    suspend fun deleteLink(itemIdA: Long, itemIdB: Long) {
        val (a, b) = normalize(itemIdA, itemIdB)
        deleteLinkEntity(itemIdA = a, itemIdB = b)
    }

    private fun normalize(itemIdA: Long, itemIdB: Long): Pair<Long, Long> =
        if (itemIdA <= itemIdB) itemIdA to itemIdB else itemIdB to itemIdA
}
