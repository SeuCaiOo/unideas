package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository

/** Renames an existing [Tag]. */
class RenameTagUseCase(private val repository: TagRepository) {

    suspend operator fun invoke(tag: Tag): Result<Unit> = runCatching {
        require(tag.name.isNotBlank()) { "Name is required" }
        repository.updateTag(tag)
    }
}
