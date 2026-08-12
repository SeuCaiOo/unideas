package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Pins or unpins an [com.seucaio.unideas.domain.model.Item] — pinned items appear in the priority panel regardless of urgency. */
class SetItemPinnedUseCase(private val repository: ItemRepository) : UseCase {

    suspend operator fun invoke(id: Long, isPinned: Boolean): Result<Unit> = resultCatching {
        repository.setItemPinned(id, isPinned)
    }
}
