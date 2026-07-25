package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Updates an existing [Item]. */
class EditItemUseCase(private val repository: ItemRepository) : UseCase {

    suspend operator fun invoke(item: Item): Result<Unit> = resultCatching {
        require(item.title.isNotBlank()) { "Title is required" }
        repository.updateItem(item)
    }
}
