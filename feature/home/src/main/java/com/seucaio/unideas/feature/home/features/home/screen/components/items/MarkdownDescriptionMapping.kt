package com.seucaio.unideas.feature.home.features.home.screen.components.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import com.mikepenz.markdown.annotator.DefaultAnnotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.model.markdownAnnotator

/**
 * Resolves inline markdown (bold/italic/strikethrough/code/links) in [description] into an
 * [AnnotatedString] — same underlying parser as the item detail screen's read-only preview
 * (`TitleDescriptionFields`), just the lower-level annotator instead of the full block-level
 * `Markdown()` composable, which can't be truncated to a fixed line count. Only the first
 * paragraph is resolved (the library's own limitation) — enough for a list-item preview capped
 * at 5 lines; block-level constructs (headings, lists, checkboxes) render as their raw characters.
 */
@Composable
internal fun markdownDescriptionAnnotatedString(description: String?): AnnotatedString? {
    if (description.isNullOrBlank()) return null
    val settings = DefaultAnnotatorSettings(
        linkTextSpanStyle = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary)),
        codeSpanStyle = SpanStyle(fontFamily = FontFamily.Monospace),
        annotator = markdownAnnotator(),
    )
    return description.buildMarkdownAnnotatedString(
        style = MaterialTheme.typography.bodyMedium,
        annotatorSettings = settings,
    )
}
