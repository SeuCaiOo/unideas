package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/** Observes all tags. */
class GetTagsUseCase(private val repository: TagRepository) : UseCase {

    operator fun invoke(): Flow<List<Tag>> = repository.getTags().logOnError(this)
}
