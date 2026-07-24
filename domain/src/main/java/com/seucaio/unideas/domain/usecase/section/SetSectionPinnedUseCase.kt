package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.repository.SectionRepository

/** Pins or unpins a [com.seucaio.unideas.domain.model.Section] — pinned sections sort first. */
class SetSectionPinnedUseCase(private val repository: SectionRepository) {

    suspend operator fun invoke(id: Long, isPinned: Boolean): Result<Unit> = runCatching {
        repository.setSectionPinned(id, isPinned)
    }
}
