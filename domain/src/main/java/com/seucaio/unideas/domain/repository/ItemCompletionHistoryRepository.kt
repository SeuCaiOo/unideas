package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import kotlinx.coroutines.flow.Flow

/** Contract for a recurring item's occurrence history. Implemented in `:data` (Room). */
interface ItemCompletionHistoryRepository {

    /** Observes every recorded occurrence of [itemId], most recent [ItemCompletionHistory.scheduledDate] first. */
    fun getHistory(itemId: Long): Flow<List<ItemCompletionHistory>>

    /** Inserts [record] and returns the generated id. */
    suspend fun insert(record: ItemCompletionHistory): Long
}
