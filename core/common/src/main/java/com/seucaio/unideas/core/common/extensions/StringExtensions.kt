package com.seucaio.unideas.core.common.extensions

/** The empty string, accessible as `String.EMPTY`. */
val String.Companion.EMPTY: String
    get() = ""

/** Returns this string, or [default] when null. */
fun String?.orDefault(default: String): String = this ?: default

private val MARKDOWN_MARKER_REGEX = Regex("[*_~`#]")
private val WHITESPACE_REGEX = Regex("\\s+")

/** Strips common inline Markdown markers and collapses whitespace — a plain-text preview, not a full renderer. */
fun String.stripMarkdownPreview(maxLength: Int): String {
    val plain = this
        .replace(MARKDOWN_MARKER_REGEX, "")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
    return if (plain.length > maxLength) {
        plain.take(maxLength).trimEnd() + "…"
    } else {
        plain
    }
}
