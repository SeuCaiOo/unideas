package com.seucaio.unideas.data.repository

import com.seucaio.unideas.core.common.extensions.toEpochMilli
import com.seucaio.unideas.data.local.dao.ItemCompletionHistoryDao
import com.seucaio.unideas.data.mapper.toDomain
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ItemCompletionHistoryRepositoryImpl(
    private val dao: ItemCompletionHistoryDao,
) : ItemCompletionHistoryRepository {

    override fun getHistory(itemId: Long): Flow<List<ItemCompletionHistory>> =
        dao.getHistory(itemId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun insert(record: ItemCompletionHistory): Long = dao.insert(record.toEntity())

    override suspend fun deleteOccurrence(itemId: Long, scheduledDate: LocalDate) =
        dao.deleteOccurrence(itemId, scheduledDate.toEpochMilli())
}
