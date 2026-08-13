package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching
import java.time.LocalDate
import java.time.LocalDateTime

class CompleteItemUseCase(
    private val repository: ItemRepository,
    private val historyRepository: ItemCompletionHistoryRepository,
    private val reminderRefreshTrigger: ReminderRefreshTrigger,
) : UseCase {

    suspend operator fun invoke(
        item: Item,
        completedAt: LocalDateTime,
        note: String? = null,
    ): Result<CompletionResult> =
        resultCatching {
            require(item.type == ItemType.TASK) { "Only tasks can be completed" }

            val result = when {
                item.isRecurring && item.dueDate != null ->
                    toggleOccurrence(item, item.dueDate, completedAt, note)

                item.isCompleted -> {
                    repository.updateItem(item.copy(completedAt = null))
                    CompletionResult.Uncompleted
                }

                else -> {
                    repository.updateItem(item.copy(completedAt = completedAt))
                    CompletionResult.Completed
                }
            }
            reminderRefreshTrigger.refreshNow()
            result
        }

    private suspend fun toggleOccurrence(
        item: Item,
        scheduledDate: LocalDate,
        completedAt: LocalDateTime,
        note: String?,
    ): CompletionResult =
        if (item.isCompleted) {
            historyRepository.deleteOccurrence(item.id, scheduledDate)
            repository.updateItem(item.copy(lastCompletedScheduledDate = null))
            CompletionResult.Uncompleted
        } else {
            val isLate = completedAt.toLocalDate().isAfter(scheduledDate)
            require(!isLate || !note.isNullOrBlank()) { "A note is required to complete a late occurrence" }
            historyRepository.insert(
                ItemCompletionHistory(
                    itemId = item.id,
                    scheduledDate = scheduledDate,
                    completedAt = completedAt,
                    note = note,
                ),
            )
            repository.updateItem(item.copy(lastCompletedScheduledDate = scheduledDate))
            CompletionResult.Completed
        }
}
