package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Renames an existing [Section]. */
class RenameSectionUseCase(private val repository: SectionRepository) : UseCase {

    suspend operator fun invoke(section: Section): Result<Unit> = resultCatching {
        require(section.name.isNotBlank()) { "Name is required" }
        repository.updateSection(section)
    }
}
