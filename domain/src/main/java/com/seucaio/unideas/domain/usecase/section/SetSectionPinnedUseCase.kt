package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Pins or unpins a [com.seucaio.unideas.domain.model.Section] — pinned sections sort first. */
class SetSectionPinnedUseCase(private val repository: SectionRepository) : UseCase {

    suspend operator fun invoke(id: Long, isPinned: Boolean): Result<Unit> = resultCatching {
        repository.setSectionPinned(id, isPinned)
    }
}
