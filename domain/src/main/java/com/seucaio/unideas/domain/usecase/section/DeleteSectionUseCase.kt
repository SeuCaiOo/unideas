package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.repository.SectionRepository

/**
 * Deletes the section with [id]; blocked if items are still linked to it.
 */
class DeleteSectionUseCase(private val repository: SectionRepository) {

    suspend operator fun invoke(id: Long): Result<DeletionStatus> = runCatching {
        val count = repository.countLinkedItems(id)
        if (count > 0) {
            DeletionStatus.BlockedByLinkedItems(count)
        } else {
            repository.deleteSection(id)
            DeletionStatus.Deleted
        }
    }
}
