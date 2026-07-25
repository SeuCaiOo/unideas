package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Creates a new [Tag], returning the generated id. */
class AddTagUseCase(private val repository: TagRepository) : UseCase {

    suspend operator fun invoke(name: String): Result<Long> = resultCatching {
        require(name.isNotBlank()) { "Name is required" }
        repository.insertTag(Tag(name = name))
    }
}
