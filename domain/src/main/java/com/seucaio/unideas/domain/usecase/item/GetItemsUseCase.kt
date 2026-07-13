package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

/** Observes items of a Home tab [type], optionally filtered by section and/or tags. */
class GetItemsUseCase(private val repository: ItemRepository) {

    operator fun invoke(
        type: ItemType,
        sectionId: Long? = null,
        tagIds: List<Long> = emptyList(),
    ): Flow<List<Item>> = repository.getItems(type, sectionId, tagIds)
}
