package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownSyntaxInserterTest {

    @Test
    fun `when bold applied with selection should wrap selected text`() {
        val value = TextFieldValue(text = "hello world", selection = TextRange(0, 5))

        val result = applyMarkdownFormat(value, MarkdownFormat.BOLD)

        assertEquals("**hello** world", result.text)
        assertEquals(TextRange(2, 7), result.selection)
    }

    @Test
    fun `when italic applied without selection should insert markers around cursor`() {
        val value = TextFieldValue(text = "hello world", selection = TextRange(5))

        val result = applyMarkdownFormat(value, MarkdownFormat.ITALIC)

        assertEquals("hello** world", result.text)
        assertEquals(TextRange(6), result.selection)
    }

    @Test
    fun `when bold applied to text already surrounded by markers should unwrap it`() {
        val value = TextFieldValue(text = "**hello** world", selection = TextRange(2, 7))

        val result = applyMarkdownFormat(value, MarkdownFormat.BOLD)

        assertEquals("hello world", result.text)
        assertEquals(TextRange(0, 5), result.selection)
    }

    @Test
    fun `when bold applied to selection that includes the markers should unwrap it`() {
        val value = TextFieldValue(text = "**hello** world", selection = TextRange(0, 9))

        val result = applyMarkdownFormat(value, MarkdownFormat.BOLD)

        assertEquals("hello world", result.text)
        assertEquals(TextRange(0, 5), result.selection)
    }

    @Test
    fun `when italic applied to text already surrounded by markers should unwrap it`() {
        val value = TextFieldValue(text = "*hello* world", selection = TextRange(1, 6))

        val result = applyMarkdownFormat(value, MarkdownFormat.ITALIC)

        assertEquals("hello world", result.text)
        assertEquals(TextRange(0, 5), result.selection)
    }

    @Test
    fun `when strikethrough applied with selection should wrap with double tilde`() {
        val value = TextFieldValue(text = "done task", selection = TextRange(0, 4))

        val result = applyMarkdownFormat(value, MarkdownFormat.STRIKETHROUGH)

        assertEquals("~~done~~ task", result.text)
        assertEquals(TextRange(2, 6), result.selection)
    }

    @Test
    fun `when bullet list applied on first line should prefix line start`() {
        val value = TextFieldValue(text = "buy milk", selection = TextRange(3))

        val result = applyMarkdownFormat(value, MarkdownFormat.BULLET_LIST)

        assertEquals("- buy milk", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun `when numbered list applied on second line should prefix that line only`() {
        val value = TextFieldValue(text = "first\nsecond", selection = TextRange(9))

        val result = applyMarkdownFormat(value, MarkdownFormat.NUMBERED_LIST)

        assertEquals("first\n1. second", result.text)
        assertEquals(TextRange(12), result.selection)
    }

    @Test
    fun `when checklist applied should prefix line with unchecked box`() {
        val value = TextFieldValue(text = "call dentist", selection = TextRange(0))

        val result = applyMarkdownFormat(value, MarkdownFormat.CHECKLIST)

        assertEquals("- [ ] call dentist", result.text)
        assertEquals(TextRange(6), result.selection)
    }
}
