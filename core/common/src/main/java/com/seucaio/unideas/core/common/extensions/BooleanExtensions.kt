package com.seucaio.unideas.core.common.extensions

/** Returns this value, or `false` when null. */
fun Boolean?.orFalse(): Boolean = this ?: false

/** Returns this value, or `true` when null. */
fun Boolean?.orTrue(): Boolean = this ?: true
