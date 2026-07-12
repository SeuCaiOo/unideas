package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

/** Observes a single [Item] by [id], `null` when it does not exist. */
class GetItemUseCase(private val repository: ItemRepository) {

    operator fun invoke(id: Long): Flow<Item?> = repository.getItem(id)
}
