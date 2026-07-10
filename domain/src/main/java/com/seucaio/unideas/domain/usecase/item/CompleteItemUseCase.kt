package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.repository.ItemRepository
import java.time.LocalDateTime

/**
 * Completes a task; if it's recurring, spawns a new instance whose due date
 * is the recurrence's next date from the completed item's **original**
 * due date (calendar cadence, independent of when it was actually completed).
 */
class CompleteItemUseCase(private val repository: ItemRepository) {

    suspend operator fun invoke(item: Item, completedAt: LocalDateTime): Result<CompletionResult> = runCatching {
        require(item.type == ItemType.TASK) { "Only tasks can be completed" }

        repository.updateItem(item.copy(completedAt = completedAt))

        val nextDueDate = item.dueDate?.let(item.recurrence::nextDueDate)
        if (nextDueDate == null) {
            CompletionResult.Completed
        } else {
            val newId = repository.insertItem(
                item.copy(id = 0L, dueDate = nextDueDate, completedAt = null, createdAt = completedAt),
            )
            CompletionResult.CompletedAndRenewed(newId)
        }
    }
}
