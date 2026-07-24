package com.seucaio.unideas.domain.util

import com.seucaio.unideas.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

/**
 * Drop-in replacement for `runCatching { block() }` — every use case's single choke point for
 * repository/DAO failures. Logs via Timber (Logcat in debug, Crashlytics in release) so a failure
 * degrading to a UI error state still leaves a trace, except [IllegalArgumentException] — that's
 * an expected `require()` validation miss, not a bug. Extension on [UseCase] so the Timber tag is
 * always the calling use case's own name, read off `this`.
 */
inline fun <T> UseCase.resultCatching(block: () -> T): Result<T> =
    runCatching(block).onFailure { error ->
        if (error !is IllegalArgumentException) Timber.tag(tagName()).e(error)
    }

/**
 * Flow counterpart of [resultCatching] — logs then rethrows, so [owner]'s own `.catch` still
 * degrades the UI as before. [owner] is `this` at the call site — a `Flow`'s receiver can't also
 * carry the enclosing class's `this`, so it's passed explicitly.
 */
fun <T> Flow<T>.logOnError(owner: UseCase): Flow<T> = catch { error ->
    Timber.tag(owner.tagName()).e(error)
    throw error
}

/**
 * Simple class name for a Timber tag — idiomatic Kotlin `this::class.simpleName` over
 * `javaClass.simpleName`. It's nullable (unlike its Java counterpart), but the fallback is honest
 * now that this is scoped to [UseCase]: any receiver here really is a use case, anonymous or not.
 */
fun UseCase.tagName(): String = this::class.simpleName ?: "UseCase"
