package com.seucaio.unideas.domain.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

/**
 * Drop-in replacement for `runCatching { block() }` — every use case's single choke point for
 * repository/DAO failures. Logs via Timber (Logcat in debug, Crashlytics in release) so a failure
 * degrading to a UI error state still leaves a trace, except [IllegalArgumentException] — that's
 * an expected `require()` validation miss, not a bug.
 */
inline fun <T> resultCatching(tag: String, block: () -> T): Result<T> =
    runCatching(block).onFailure { error ->
        if (error !is IllegalArgumentException) Timber.e(error, tag)
    }

/**
 * Flow counterpart of [resultCatching] — logs then rethrows, so a caller's own `.catch` still
 * degrades the UI as before.
 */
fun <T> Flow<T>.logOnError(tag: String): Flow<T> = catch { error ->
    Timber.e(error, tag)
    throw error
}
