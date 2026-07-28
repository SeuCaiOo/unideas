package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/**
 * Observes every non-completed item with a due date, for the periodic reminder check (#115) —
 * unbounded by the "due soon" window that [GetPriorityItemsUseCase] uses.
 */
class GetItemsWithDueDateUseCase(private val repository: ItemRepository) : UseCase {

    operator fun invoke(): Flow<List<Item>> = repository.getItemsWithDueDate().logOnError(this)
}
