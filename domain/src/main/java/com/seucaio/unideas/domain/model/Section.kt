package com.seucaio.unideas.domain.model

/**
 * A section groups items (single choice per item). Deleting a section with
 * linked items is blocked at the use-case level (see `DeletionStatus`).
 */
data class Section(
    val id: Long = 0L,
    val name: String,
)
