package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository

/** Renames an existing [Section]. */
class RenameSectionUseCase(private val repository: SectionRepository) {

    suspend operator fun invoke(section: Section): Result<Unit> = runCatching {
        require(section.name.isNotBlank()) { "Name is required" }
        repository.updateSection(section)
    }
}
