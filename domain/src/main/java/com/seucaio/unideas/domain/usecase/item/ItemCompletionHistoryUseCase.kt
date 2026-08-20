package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import com.seucaio.unideas.domain.util.resultCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ItemCompletionHistoryUseCase(
    private val repository: ItemCompletionHistoryRepository,
) : UseCase {

    fun getHistory(itemId: Long): Flow<List<ItemCompletionHistory>> =
        repository.getHistory(itemId).logOnError(this)

    suspend fun save(record: ItemCompletionHistory): Result<Unit> =
        resultCatching {
            require(!record.scheduledDate.isAfter(LocalDate.now())) {
                "scheduledDate cannot be in the future"
            }
            val isLate = record.completedAt != null && record.completedAt.toLocalDate()
                .isAfter(record.scheduledDate)
            require(!isLate || !record.note.isNullOrBlank()) { "A note is required for a late entry" }

            val existing = repository.getHistory(record.itemId).first()
            val duplicate =
                existing.any { it.scheduledDate == record.scheduledDate && it.id != record.id }
            require(!duplicate) { "An entry already exists for this date" }

            if (record.id == 0L) repository.insert(record) else repository.update(record)
            Unit
        }

    suspend fun delete(id: Long): Result<Unit> = resultCatching { repository.deleteById(id) }
}
