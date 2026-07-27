package com.seucaio.unideas.feature.items.ui.components.fields.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private val boldMarkerRegex = Regex("""\*\*(.+?)\*\*""")
private val italicMarkerRegex = Regex("""_(.+?)_""")
private val strikethroughMarkerRegex = Regex("""~~(.+?)~~""")
private val listMarkerRegex = Regex("""(?m)^\s*([-*]|\d+\.)(\s\[[ xX]])?\s""")

/**
 * WhatsApp-style syntax dimming: the `**`/`_`/`~~` markers and list/checklist prefixes get
 * [markerColor] instead of the default text color, so they read as punctuation rather than content
 * — the underlying string is untouched, only the rendered span colors change.
 */
private class MarkdownSyntaxHighlightTransformation(
    private val markerColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(highlightMarkdownSyntax(text.text, markerColor), OffsetMapping.Identity)
}

private const val MARKER_ALPHA = 0.45f

@Composable
fun rememberMarkdownSyntaxHighlightTransformation(): VisualTransformation {
    // onSurfaceVariant is too close to onSurface in this theme to read as "dimmed" — a lower
    // alpha on the same text color gives the WhatsApp-style faded-punctuation look reliably.
    val markerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = MARKER_ALPHA)
    return remember(markerColor) { MarkdownSyntaxHighlightTransformation(markerColor) }
}

/**
 * Processes a string to identify Markdown syntax markers and applies a specific color to them.
 *
 * This function uses regular expressions to find bold (**), italic (_), strikethrough (~~),
 * and list/checklist markers. It returns an [AnnotatedString] where these markers are styled
 * with the provided [markerColor], typically used to dim the syntax characters relative
 * to the actual content.
 *
 * @param text The raw input string containing Markdown syntax.
 * @param markerColor The [Color] to apply to the Markdown syntax markers.
 */
private fun highlightMarkdownSyntax(text: String, markerColor: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)

        fun dim(range: IntRange) {
            if (range.isEmpty()) return
            addStyle(SpanStyle(color = markerColor), range.first, range.last + 1)
        }

        boldMarkerRegex.findAll(text).forEach { match ->
            dim(match.range.first..match.range.first + 1)
            dim(match.range.last - 1..match.range.last)
        }
        italicMarkerRegex.findAll(text).forEach { match ->
            dim(match.range.first..match.range.first)
            dim(match.range.last..match.range.last)
        }
        strikethroughMarkerRegex.findAll(text).forEach { match ->
            dim(match.range.first..match.range.first + 1)
            dim(match.range.last - 1..match.range.last)
        }
        listMarkerRegex.findAll(text).forEach { match ->
            match.groups[1]?.let { dim(it.range) }
            match.groups[2]?.let { dim(it.range) }
        }
    }
