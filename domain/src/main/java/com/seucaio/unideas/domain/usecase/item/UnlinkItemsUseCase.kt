package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemLinkRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Removes the link between two items, if any. */
class UnlinkItemsUseCase(private val repository: ItemLinkRepository) : UseCase {

    suspend operator fun invoke(itemIdA: Long, itemIdB: Long): Result<Unit> = resultCatching {
        repository.unlinkItems(itemIdA, itemIdB)
    }
}
