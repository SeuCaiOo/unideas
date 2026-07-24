package com.seucaio.unideas.domain.usecase.tag

import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Renames an existing [Tag]. */
class RenameTagUseCase(private val repository: TagRepository) : UseCase {

    suspend operator fun invoke(tag: Tag): Result<Unit> = resultCatching {
        require(tag.name.isNotBlank()) { "Name is required" }
        repository.updateTag(tag)
    }
}
