package com.seucaio.unideas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCompletionHistoryDao {

    @Query("SELECT * FROM item_completion_history WHERE itemId = :itemId ORDER BY scheduledDate DESC")
    fun getHistory(itemId: Long): Flow<List<ItemCompletionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ItemCompletionHistoryEntity): Long
}
