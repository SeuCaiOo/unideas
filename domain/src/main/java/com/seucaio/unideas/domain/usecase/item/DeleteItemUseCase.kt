package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository

/** Deletes the item with [id]. */
class DeleteItemUseCase(private val repository: ItemRepository) {

    suspend operator fun invoke(id: Long) = repository.deleteItem(id)
}
