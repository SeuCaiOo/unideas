package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.core.common.crud.EntityCrudOperations
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import kotlinx.coroutines.flow.Flow

/** Adapts the Section use cases to the generic [EntityCrudOperations] contract. */
class SectionCrudOperations(
    private val getSections: GetSectionsUseCase,
    private val addSection: AddSectionUseCase,
    private val renameSection: RenameSectionUseCase,
    private val deleteSection: DeleteSectionUseCase,
) : EntityCrudOperations<Section> {

    override fun getAll(): Flow<List<Section>> = getSections()

    override suspend fun add(name: String): Result<Long> = addSection(name)

    override suspend fun rename(item: Section, newName: String): Result<Unit> =
        renameSection(item.copy(name = newName))

    override suspend fun delete(item: Section): Result<DeletionStatus> = deleteSection(item.id)
}
