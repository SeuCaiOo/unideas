package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/**
 * Deletes the section with [id]; blocked if items are still linked to it.
 */
class DeleteSectionUseCase(private val repository: SectionRepository) : UseCase {

    suspend operator fun invoke(id: Long): Result<DeletionStatus> = resultCatching {
        val count = repository.countLinkedItems(id)
        if (count > 0) {
            DeletionStatus.BlockedByLinkedItems(count)
        } else {
            repository.deleteSection(id)
            DeletionStatus.Deleted
        }
    }
}
