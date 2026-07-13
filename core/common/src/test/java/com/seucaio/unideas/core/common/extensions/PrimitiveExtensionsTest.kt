package com.seucaio.unideas.core.common.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimitiveExtensionsTest {

    @Test
    fun `orFalse returns value when not null and false when null`() {
        assertTrue(true.orFalse())
        assertFalse(false.orFalse())
        assertFalse((null as Boolean?).orFalse())
    }

    @Test
    fun `orTrue returns value when not null and true when null`() {
        assertTrue(true.orTrue())
        assertFalse(false.orTrue())
        assertTrue((null as Boolean?).orTrue())
    }

    @Test
    fun `String EMPTY is the empty string`() {
        assertEquals("", String.EMPTY)
    }

    @Test
    fun `orDefault returns value when not null and default when null`() {
        assertEquals("value", "value".orDefault("default"))
        assertEquals("default", (null as String?).orDefault("default"))
    }

    @Test
    fun `orZero returns value when not null and zero when null`() {
        assertEquals(42L, 42L.orZero())
        assertEquals(0L, (null as Long?).orZero())
    }

    @Test
    fun `isZero and isNotZero`() {
        assertTrue(0L.isZero())
        assertFalse(1L.isZero())
        assertTrue(1L.isNotZero())
        assertTrue((-1L).isNotZero())
        assertFalse(0L.isNotZero())
    }
}
