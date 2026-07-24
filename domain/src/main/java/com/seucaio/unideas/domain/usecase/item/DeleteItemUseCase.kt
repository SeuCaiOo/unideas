package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Deletes the item with [id]. */
class DeleteItemUseCase(private val repository: ItemRepository) : UseCase {

    suspend operator fun invoke(id: Long): Result<Unit> = resultCatching {
        repository.deleteItem(id)
    }
}
