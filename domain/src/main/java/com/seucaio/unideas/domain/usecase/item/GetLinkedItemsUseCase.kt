package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemLinkRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/** Observes every item linked to [itemId]. */
class GetLinkedItemsUseCase(private val repository: ItemLinkRepository) : UseCase {

    operator fun invoke(itemId: Long): Flow<List<Item>> = repository.getLinkedItems(itemId).logOnError(this)
}
