package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun applyMarkdownFormat(value: TextFieldValue, format: MarkdownFormat): TextFieldValue = when (format) {
    MarkdownFormat.BOLD -> wrapSelection(value, "**")
    MarkdownFormat.ITALIC -> wrapSelection(value, "_")
    MarkdownFormat.STRIKETHROUGH -> wrapSelection(value, "~~")
    MarkdownFormat.BULLET_LIST -> prefixLine(value, "- ")
    MarkdownFormat.NUMBERED_LIST -> prefixLine(value, "1. ")
    MarkdownFormat.CHECKLIST -> prefixLine(value, "- [ ] ")
}

private fun wrapSelection(value: TextFieldValue, marker: String): TextFieldValue {
    val text = value.text
    val start = value.selection.min
    val end = value.selection.max

    val surroundedByMarker = start >= marker.length && end + marker.length <= text.length &&
        text.regionMatches(start - marker.length, marker, 0, marker.length) &&
        text.regionMatches(end, marker, 0, marker.length)
    val wrappedByMarker = end - start >= marker.length * 2 &&
        text.regionMatches(start, marker, 0, marker.length) &&
        text.regionMatches(end - marker.length, marker, 0, marker.length)

    return when {
        surroundedByMarker -> {
            val newText = text.substring(0, start - marker.length) + text.substring(start, end) +
                text.substring(end + marker.length)
            value.copy(text = newText, selection = TextRange(start - marker.length, end - marker.length))
        }
        wrappedByMarker -> {
            val newText = text.substring(0, start) + text.substring(start + marker.length, end - marker.length) +
                text.substring(end)
            value.copy(text = newText, selection = TextRange(start, end - marker.length * 2))
        }
        else -> {
            val newText = text.substring(0, start) + marker + text.substring(start, end) +
                marker + text.substring(end)
            val newSelection = if (start == end) {
                TextRange(start + marker.length)
            } else {
                TextRange(start + marker.length, end + marker.length)
            }
            value.copy(text = newText, selection = newSelection)
        }
    }
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
