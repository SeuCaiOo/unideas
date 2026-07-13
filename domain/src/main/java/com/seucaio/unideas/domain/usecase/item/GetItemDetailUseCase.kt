package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemDetail
import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

/** Observes a single [ItemDetail] (item + resolved section name) by [id], `null` if it does not exist. */
class GetItemDetailUseCase(private val repository: ItemRepository) {

    operator fun invoke(id: Long): Flow<ItemDetail?> = repository.getItemDetail(id)
}
