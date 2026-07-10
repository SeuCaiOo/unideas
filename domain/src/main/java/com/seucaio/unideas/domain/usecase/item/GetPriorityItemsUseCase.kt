package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Observes overdue + due-soon items, capped at [limit].
 *
 * [today], [dueSoonDays] and [limit] are explicit parameters — `:domain`
 * cannot depend on `:core:common`, where their configured values
 * (`Constants.DUE_SOON_DAYS`/`PRIORITY_PANEL_LIMIT`) live; the caller passes them in.
 */
class GetPriorityItemsUseCase(private val repository: ItemRepository) {

    operator fun invoke(today: LocalDate, dueSoonDays: Int, limit: Int): Flow<List<Item>> =
        repository.getPriorityItems(dueOnOrBefore = today.plusDays(dueSoonDays.toLong()))
            .map { it.take(limit) }
}
