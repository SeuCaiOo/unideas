package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.repository.TagRepository

/**
 * Deletes the tag with [id]; blocked if items are still linked to it.
 */
class DeleteTagUseCase(private val repository: TagRepository) {

    suspend operator fun invoke(id: Long): Result<DeletionStatus> = runCatching {
        val count = repository.countLinkedItems(id)
        if (count > 0) {
            DeletionStatus.BlockedByLinkedItems(count)
        } else {
            repository.deleteTag(id)
            DeletionStatus.Deleted
        }
    }
}
