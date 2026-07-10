package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository

/** Creates a new [Section], returning the generated id. */
class AddSectionUseCase(private val repository: SectionRepository) {

    suspend operator fun invoke(name: String): Result<Long> = runCatching {
        require(name.isNotBlank()) { "Name is required" }
        repository.insertSection(Section(name = name))
    }
}
