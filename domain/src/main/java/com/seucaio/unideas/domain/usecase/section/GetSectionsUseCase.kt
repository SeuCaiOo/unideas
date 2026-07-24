package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/** Observes all sections. */
class GetSectionsUseCase(private val repository: SectionRepository) : UseCase {

    operator fun invoke(): Flow<List<Section>> = repository.getSections().logOnError(this)
}
