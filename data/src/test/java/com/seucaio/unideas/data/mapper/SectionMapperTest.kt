package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.domain.stub.SectionStub
import org.junit.Assert.assertEquals
import org.junit.Test

class SectionMapperTest {

    @Test
    fun `toDomain and toEntity round-trip`() {
        val section = SectionStub.section(id = 7L, name = "Trabalho")

        val roundTripped = section.toEntity().toDomain()

        assertEquals(section, roundTripped)
    }

    @Test
    fun `toEntity maps fields directly`() {
        val section = SectionStub.section(id = 3L, name = "Casa")

        val entity = section.toEntity()

        assertEquals(3L, entity.id)
        assertEquals("Casa", entity.name)
    }
}
