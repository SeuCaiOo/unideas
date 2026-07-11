package com.seucaio.unideas.domain.usecase.section

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import kotlinx.coroutines.flow.Flow

/**
 * CRUD for [Section] behind one class, one method per operation — a convenience facade over
 * the existing single-purpose use cases (kept as-is, still usable on their own), so a ViewModel
 * that needs the full CRUD set doesn't have to carry 4 separate constructor params. Experiment
 * (2026-07-11, user request): compares this shape against the "one class per operation"
 * convention used elsewhere in `:domain`. No repository access here — every call just
 * delegates.
 */
class SectionUseCase(
    private val getSections: GetSectionsUseCase,
    private val addSection: AddSectionUseCase,
    private val renameSection: RenameSectionUseCase,
    private val deleteSection: DeleteSectionUseCase,
) {

    fun getAll(): Flow<List<Section>> = getSections()

    suspend fun add(name: String): Result<Long> = addSection(name)

    suspend fun rename(section: Section): Result<Unit> = renameSection(section)

    suspend fun delete(id: Long): Result<DeletionStatus> = deleteSection(id)
}
