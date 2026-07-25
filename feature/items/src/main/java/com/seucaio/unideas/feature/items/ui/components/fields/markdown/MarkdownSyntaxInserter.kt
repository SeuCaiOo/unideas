package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun applyMarkdownFormat(value: TextFieldValue, format: MarkdownFormat): TextFieldValue = when (format) {
    MarkdownFormat.BOLD -> wrapSelection(value, "**")
    MarkdownFormat.ITALIC -> wrapSelection(value, "*")
    MarkdownFormat.STRIKETHROUGH -> wrapSelection(value, "~~")
    MarkdownFormat.BULLET_LIST -> prefixLine(value, "- ")
    MarkdownFormat.NUMBERED_LIST -> prefixLine(value, "1. ")
    MarkdownFormat.CHECKLIST -> prefixLine(value, "- [ ] ")
}

private fun wrapSelection(value: TextFieldValue, marker: String): TextFieldValue {
    val selection = value.selection
    val start = selection.min
    val end = selection.max
    val newText = value.text.substring(0, start) + marker + value.text.substring(start, end) +
        marker + value.text.substring(end)
    val newSelection = if (start == end) {
        TextRange(start + marker.length)
    } else {
        TextRange(start + marker.length, end + marker.length)
    }
    return value.copy(text = newText, selection = newSelection)
}

private fun prefixLine(value: TextFieldValue, prefix: String): TextFieldValue {
    val cursor = value.selection.min
    val lineStart = value.text.lastIndexOf('\n', cursor - 1) + 1
    val newText = value.text.substring(0, lineStart) + prefix + value.text.substring(lineStart)
    val shift = prefix.length
    return value.copy(
        text = newText,
        selection = TextRange(value.selection.start + shift, value.selection.end + shift),
    )
}
