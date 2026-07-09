package com.seucaio.unideas.core.common.extensions

/** Returns this value, or `0L` when null. */
fun Long?.orZero(): Long = this ?: 0L

/** Returns `true` when this value is exactly zero. */
fun Long.isZero(): Boolean = this == 0L

/** Returns `true` when this value is not zero. */
fun Long.isNotZero(): Boolean = this != 0L
