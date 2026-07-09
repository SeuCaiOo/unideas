package com.seucaio.unideas.domain.model

/**
 * A tag labels items (many per item). Deleting a tag with linked items is
 * blocked at the use-case level (see `DeletionStatus`).
 */
data class Tag(
    val id: Long = 0L,
    val name: String,
)
