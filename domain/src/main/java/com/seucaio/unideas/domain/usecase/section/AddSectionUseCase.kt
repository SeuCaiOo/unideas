package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Creates a new [Section], returning the generated id. */
class AddSectionUseCase(private val repository: SectionRepository) : UseCase {

    suspend operator fun invoke(name: String): Result<Long> = resultCatching {
        require(name.isNotBlank()) { "Name is required" }
        repository.insertSection(Section(name = name))
    }
}
