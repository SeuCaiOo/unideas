package com.seucaio.unideas.domain.stub

import com.seucaio.unideas.domain.model.Section

/**
 * Shared [Section] samples for tests across modules.
 */
object SectionStub {

    fun section(
        id: Long = 1L,
        name: String = "Trabalho",
        isPinned: Boolean = false,
    ): Section = Section(id = id, name = name, isPinned = isPinned)

    fun sections(count: Int = 3): List<Section> =
        (1..count).map { section(id = it.toLong(), name = "Seção $it") }
}
