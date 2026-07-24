package com.seucaio.unideas.domain.usecase

/**
 * Marker for every use case class — no members. Scopes `resultCatching`/`logOnError`
 * (`domain/util/ResultLogging.kt`) to actual use cases instead of any `Any`, so their Timber tag
 * (the implementing class's own name) is always meaningful, never a guessed fallback.
 */
interface UseCase
