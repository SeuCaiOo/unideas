package com.seucaio.unideas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCompletionHistoryDao {

    @Query("SELECT * FROM item_completion_history WHERE itemId = :itemId ORDER BY scheduledDate DESC")
    fun getHistory(itemId: Long): Flow<List<ItemCompletionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ItemCompletionHistoryEntity): Long

    @Update
    suspend fun update(entity: ItemCompletionHistoryEntity)

    @Query("DELETE FROM item_completion_history WHERE itemId = :itemId AND scheduledDate = :scheduledDate")
    suspend fun deleteOccurrence(itemId: Long, scheduledDate: Long)

    @Query("DELETE FROM item_completion_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
