package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.domain.repository.ItemCompletionHistoryRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/** Observes a recurring item's occurrence history, for the series history bottom sheet. */
class GetItemCompletionHistoryUseCase(private val repository: ItemCompletionHistoryRepository) : UseCase {

    operator fun invoke(itemId: Long): Flow<List<ItemCompletionHistory>> =
        repository.getHistory(itemId).logOnError(this)
}
