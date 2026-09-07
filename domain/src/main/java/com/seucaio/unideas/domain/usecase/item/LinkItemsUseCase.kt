package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemLinkRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Links two items together (symmetric — order doesn't matter). */
class LinkItemsUseCase(private val repository: ItemLinkRepository) : UseCase {

    suspend operator fun invoke(itemIdA: Long, itemIdB: Long): Result<Unit> = resultCatching {
        repository.linkItems(itemIdA, itemIdB)
    }
}
