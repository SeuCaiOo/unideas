package com.seucaio.unideas.domain.stub

import com.seucaio.unideas.domain.model.Section

/**
 * Shared [Section] samples for tests across modules.
 */
object SectionStub {

    fun section(
        id: Long = 1L,
        name: String = "Trabalho",
    ): Section = Section(id = id, name = name)

    fun sections(count: Int = 3): List<Section> =
        (1..count).map { section(id = it.toLong(), name = "Seção $it") }
}
