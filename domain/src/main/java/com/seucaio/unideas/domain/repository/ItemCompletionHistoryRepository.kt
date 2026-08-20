package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Contract for a recurring item's occurrence history. Implemented in `:data` (Room). */
interface ItemCompletionHistoryRepository {

    /** Observes every recorded occurrence of [itemId], most recent [ItemCompletionHistory.scheduledDate] first. */
    fun getHistory(itemId: Long): Flow<List<ItemCompletionHistory>>

    /** Inserts [record] and returns the generated id. */
    suspend fun insert(record: ItemCompletionHistory): Long

    /** Updates an existing [record] (matched by [ItemCompletionHistory.id]). */
    suspend fun update(record: ItemCompletionHistory)

    /** Removes the occurrence recorded for [itemId] at [scheduledDate], if any — undoes a completion. */
    suspend fun deleteOccurrence(itemId: Long, scheduledDate: LocalDate)

    /** Removes the entry identified by [id]. */
    suspend fun deleteById(id: Long)
}
