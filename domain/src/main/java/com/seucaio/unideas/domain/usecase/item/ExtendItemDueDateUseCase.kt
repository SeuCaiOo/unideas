package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching
import java.time.LocalDate

class ExtendItemDueDateUseCase(
    private val repository: ItemRepository,
    private val reminderRefreshTrigger: ReminderRefreshTrigger,
) : UseCase {

    suspend operator fun invoke(item: Item, newDueDate: LocalDate, today: LocalDate): Result<Item> =
        resultCatching {
            val dueDate = requireNotNull(item.dueDate) { "Item has no dueDate to extend" }
            require(dueDate.isBefore(today)) { "Only an overdue item's dueDate can be extended" }
            require(newDueDate.isAfter(dueDate)) { "newDueDate must be after the current dueDate" }

            val extended = item.copy(
                dueDate = newDueDate,
                pendingExtensionOriginalDueDate = item.pendingExtensionOriginalDueDate ?: dueDate,
                pendingExtensionCount = item.pendingExtensionCount + 1,
            )
            repository.updateItem(extended)
            reminderRefreshTrigger.refreshNow()
            extended
        }
}
