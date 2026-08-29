package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.AutoBackupTrigger
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching
import java.time.LocalDate

class IgnoreOccurrenceUseCase(
    private val repository: ItemRepository,
    private val historyRepository: ItemCompletionHistoryRepository,
    private val reminderRefreshTrigger: ReminderRefreshTrigger,
    private val autoBackupTrigger: AutoBackupTrigger,
) : UseCase {

    suspend operator fun invoke(item: Item, note: String, today: LocalDate): Result<Item> =
        resultCatching {
            require(item.isRecurring) { "Only a recurring item's occurrence can be ignored" }
            val dueDate = requireNotNull(item.dueDate) { "Item has no dueDate to ignore" }
            require(!dueDate.isAfter(today)) { "Only a due or overdue occurrence can be ignored" }
            require(note.isNotBlank()) { "A note is required to ignore an occurrence" }

            historyRepository.insert(
                ItemCompletionHistory(
                    itemId = item.id,
                    scheduledDate = dueDate,
                    completedAt = null,
                    note = note,
                    originalScheduledDate = item.pendingExtensionOriginalDueDate,
                    extensionCount = item.pendingExtensionCount,
                ),
            )
            val nextDueDate = requireNotNull(item.recurrence.nextDueDate(dueDate))
            val advanced = item.copy(
                dueDate = nextDueDate,
                pendingExtensionOriginalDueDate = null,
                pendingExtensionCount = 0,
            )
            repository.updateItem(advanced)
            reminderRefreshTrigger.refreshNow()
            autoBackupTrigger.triggerNow()
            advanced
        }
}
