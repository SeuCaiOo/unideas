package com.seucaio.unideas.domain.repository

/**
 * Requests an out-of-cycle run of the periodic reminder check (#115) — e.g. right after
 * completing an item, so its notification clears immediately instead of waiting for the next
 * scheduled check (up to 6h away). Implemented in `:core:notifications` (WorkManager), injected
 * via DI; fire-and-forget, no result to await.
 */
interface ReminderRefreshTrigger {

    fun refreshNow()
}
