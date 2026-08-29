package com.seucaio.unideas.domain.repository

/**
 * Requests an out-of-cycle automatic Drive backup (#193) — same sync points as
 * [ReminderRefreshTrigger]: item completed/ignored/extended, created/edited, or a manual
 * pull-to-refresh. Implemented in `:core:backup`, injected via DI; fire-and-forget, no result to
 * await — a no-op internally when the auto-backup preference is off or no account is connected.
 */
interface AutoBackupTrigger {

    fun triggerNow()
}
