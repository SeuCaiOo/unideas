package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository

/** Updates an existing [Item]. */
class EditItemUseCase(private val repository: ItemRepository) {

    suspend operator fun invoke(item: Item): Result<Unit> = runCatching {
        require(item.title.isNotBlank()) { "Title is required" }
        repository.updateItem(item)
    }
}
