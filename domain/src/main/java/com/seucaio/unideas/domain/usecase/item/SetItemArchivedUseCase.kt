package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Archives or unarchives an [com.seucaio.unideas.domain.model.Item] — an archived item is hidden from the normal listing and its reminders pause. */
class SetItemArchivedUseCase(private val repository: ItemRepository) : UseCase {

    suspend operator fun invoke(id: Long, archived: Boolean): Result<Unit> = resultCatching {
        repository.setItemStatus(id, if (archived) ItemStatus.ARCHIVED else ItemStatus.ACTIVE)
    }
}
