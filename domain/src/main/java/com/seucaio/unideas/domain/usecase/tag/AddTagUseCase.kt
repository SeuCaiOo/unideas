package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository

/** Creates a new [Tag], returning the generated id. */
class AddTagUseCase(private val repository: TagRepository) {

    suspend operator fun invoke(name: String): Result<Long> = runCatching {
        require(name.isNotBlank()) { "Name is required" }
        repository.insertTag(Tag(name = name))
    }
}
