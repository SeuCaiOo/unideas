package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow

/** Observes all tags. */
class GetTagsUseCase(private val repository: TagRepository) {

    operator fun invoke(): Flow<List<Tag>> = repository.getTags()
}
