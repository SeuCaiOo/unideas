package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository

/** Creates a new [Item], returning the generated id. */
class CreateItemUseCase(private val repository: ItemRepository) {

    suspend operator fun invoke(item: Item): Result<Long> = runCatching {
        require(item.title.isNotBlank()) { "Title is required" }
        repository.insertItem(item)
    }
}
