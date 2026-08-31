package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class GetPriorityItemsUseCase(private val repository: ItemRepository) : UseCase {

    operator fun invoke(today: LocalDate, dueSoonDays: Int): Flow<List<Item>> =
        repository.getPriorityItems(dueOnOrBefore = today.plusDays(dueSoonDays.toLong()))
            .map { items -> items.filterNot { it.isCompleted } }
            .logOnError(this)
}
