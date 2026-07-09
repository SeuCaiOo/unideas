package com.seucaio.unideas.domain.stub

import com.seucaio.unideas.domain.model.Tag

/**
 * Shared [Tag] samples for tests across modules.
 */
object TagStub {

    fun tag(
        id: Long = 1L,
        name: String = "urgente",
    ): Tag = Tag(id = id, name = name)

    fun tags(count: Int = 3): List<Tag> =
        (1..count).map { tag(id = it.toLong(), name = "tag-$it") }
}
