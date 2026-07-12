package com.seucaio.unideas.domain.model

/**
 * [item] together with its [sectionName], resolved once in `:data` (via the
 * `ItemWithTagsAndSection` Room relation) — the read-only detail screen needs a label, and
 * [Item.sectionId] alone isn't one.
 */
data class ItemDetail(
    val item: Item,
    val sectionName: String?,
)
