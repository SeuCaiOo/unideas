package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Observes overdue + due-soon items, ordered by due date, uncapped — the caller (Home) decides
 * how many to show in the fixed panel and whether "Ver todas" is needed, since only it knows
 * the panel's display limit.
 *
 * [today] and [dueSoonDays] are explicit parameters — `:domain` cannot depend on `:core:common`,
 * where their configured values (`Constants.DUE_SOON_DAYS`) live; the caller passes them in.
 */
class GetPriorityItemsUseCase(private val repository: ItemRepository) {

    operator fun invoke(today: LocalDate, dueSoonDays: Int): Flow<List<Item>> =
        repository.getPriorityItems(dueOnOrBefore = today.plusDays(dueSoonDays.toLong()))
}
