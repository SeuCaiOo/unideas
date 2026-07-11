package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.core.common.crud.EntityCrudOperations
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import kotlinx.coroutines.flow.Flow

/** Adapts the Tag use cases to the generic [EntityCrudOperations] contract. */
class TagCrudOperations(
    private val getTags: GetTagsUseCase,
    private val addTag: AddTagUseCase,
    private val renameTag: RenameTagUseCase,
    private val deleteTag: DeleteTagUseCase,
) : EntityCrudOperations<Tag> {

    override fun getAll(): Flow<List<Tag>> = getTags()

    override suspend fun add(name: String): Result<Long> = addTag(name)

    override suspend fun rename(item: Tag, newName: String): Result<Unit> =
        renameTag(item.copy(name = newName))

    override suspend fun delete(item: Tag): Result<DeletionStatus> = deleteTag(item.id)
}
