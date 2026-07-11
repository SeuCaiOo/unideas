package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import kotlinx.coroutines.flow.Flow

/**
 * CRUD for [Tag] behind one class, one method per operation — a convenience facade over the
 * existing single-purpose use cases (kept as-is, still usable on their own), so a ViewModel
 * that needs the full CRUD set doesn't have to carry 4 separate constructor params. Same shape
 * as [com.seucaio.unideas.domain.usecase.section.SectionUseCase] (2026-07-11 experiment,
 * applied here after that one was approved). No repository access here — every call just
 * delegates.
 */
class TagUseCase(
    private val getTags: GetTagsUseCase,
    private val addTag: AddTagUseCase,
    private val renameTag: RenameTagUseCase,
    private val deleteTag: DeleteTagUseCase,
) {

    fun getAll(): Flow<List<Tag>> = getTags()

    suspend fun add(name: String): Result<Long> = addTag(name)

    suspend fun rename(tag: Tag): Result<Unit> = renameTag(tag)

    suspend fun delete(id: Long): Result<DeletionStatus> = deleteTag(id)
}
