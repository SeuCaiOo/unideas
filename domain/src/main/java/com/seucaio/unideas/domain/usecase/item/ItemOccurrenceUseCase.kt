package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class ItemOccurrenceUseCase(
    private val completeItem: CompleteItemUseCase,
    private val itemCompletionHistoryUseCase: ItemCompletionHistoryUseCase,
    private val ignoreOccurrenceUseCase: IgnoreOccurrenceUseCase,
    private val extendItemDueDateUseCase: ExtendItemDueDateUseCase,
) {

    suspend fun complete(
        item: Item,
        completedAt: LocalDateTime,
        note: String? = null
    ): Result<CompletionResult> =
        completeItem(item, completedAt, note)

    fun getHistory(itemId: Long): Flow<List<ItemCompletionHistory>> =
        itemCompletionHistoryUseCase.getHistory(itemId)

    suspend fun ignore(item: Item, note: String, today: LocalDate = LocalDate.now()): Result<Item> =
        ignoreOccurrenceUseCase(item, note, today)

    suspend fun extendDueDate(
        item: Item,
        newDueDate: LocalDate,
        today: LocalDate = LocalDate.now()
    ): Result<Item> =
        extendItemDueDateUseCase(item, newDueDate, today)

    suspend fun saveHistoryEntry(record: ItemCompletionHistory): Result<Unit> =
        itemCompletionHistoryUseCase.save(record)

    suspend fun deleteHistoryEntry(id: Long): Result<Unit> =
        itemCompletionHistoryUseCase.delete(id)
}
