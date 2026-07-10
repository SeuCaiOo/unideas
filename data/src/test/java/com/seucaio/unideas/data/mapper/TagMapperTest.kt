package com.seucaio.unideas.data.mapper

import com.seucaio.unideas.domain.stub.TagStub
import org.junit.Assert.assertEquals
import org.junit.Test

class TagMapperTest {

    @Test
    fun `toDomain and toEntity round-trip`() {
        val tag = TagStub.tag(id = 7L, name = "urgente")

        val roundTripped = tag.toEntity().toDomain()

        assertEquals(tag, roundTripped)
    }

    @Test
    fun `toEntity maps fields directly`() {
        val tag = TagStub.tag(id = 3L, name = "casa")

        val entity = tag.toEntity()

        assertEquals(3L, entity.id)
        assertEquals("casa", entity.name)
    }
}
