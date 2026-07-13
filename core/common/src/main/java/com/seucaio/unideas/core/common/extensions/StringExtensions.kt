package com.seucaio.unideas.core.common.extensions

/** The empty string, accessible as `String.EMPTY`. */
val String.Companion.EMPTY: String
    get() = ""

/** Returns this string, or [default] when null. */
fun String?.orDefault(default: String): String = this ?: default
