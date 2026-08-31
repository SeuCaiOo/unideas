package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetItemsUseCase(private val repository: ItemRepository) : UseCase {

    operator fun invoke(
        type: ItemType,
        sectionId: Long? = null,
        tagIds: List<Long> = emptyList(),
    ): Flow<List<Item>> = repository.getItems(type, sectionId, tagIds)
        .map { items -> items.sortedBy { it.isCompleted } }
        .logOnError(this)
}
