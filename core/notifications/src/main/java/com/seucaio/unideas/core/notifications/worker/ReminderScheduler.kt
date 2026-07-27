package com.seucaio.unideas.core.notifications.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules [ReminderCheckWorker] to run 4x/day (00h/06h/12h/18h) — see [CHECK_HOURS]. WorkManager
 * has no native support for "run at specific times of day", only intervals, so this uses a
 * [androidx.work.PeriodicWorkRequest] at [CHECK_INTERVAL_HOURS] with an initial delay computed to
 * land on the next slot.
 */
object ReminderScheduler {

    internal val CHECK_HOURS = listOf(0, 6, 12, 18)
    internal const val KEY_SILENT = "silent"
    private const val CHECK_INTERVAL_HOURS = 6L
    private const val PERIODIC_WORK_NAME = "reminder_check"
    private const val REFRESH_WORK_NAME = "reminder_check_refresh"

    /** Enqueues the periodic check if not already scheduled — safe to call on every app start. */
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        val initialDelay = Duration.between(now, nextCheckSlot(now))

        val request =
            PeriodicWorkRequestBuilder<ReminderCheckWorker>(CHECK_INTERVAL_HOURS, TimeUnit.HOURS)
                .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /**
     * Requests an immediate out-of-cycle run. [silent] distinguishes *why* it's running: a
     * completion-triggered refresh (e.g. [com.seucaio.unideas.core.notifications.worker.ReminderRefreshTriggerImpl])
     * didn't discover anything new — it's just bookkeeping after the user resolved an item — so it
     * shouldn't re-alert (sound/vibration) a notification the user already dismissed. The debug
     * "run reminder check now" button in Settings passes `silent = false`, since it's meant to
     * simulate a real periodic check end-to-end.
     */
    fun refreshNow(context: Context, silent: Boolean) {
        val request = OneTimeWorkRequestBuilder<ReminderCheckWorker>()
            .setInputData(Data.Builder().putBoolean(KEY_SILENT, silent).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(REFRESH_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    /** The next check slot ([CHECK_HOURS]) strictly after [from]. */
    internal fun nextCheckSlot(from: LocalDateTime): LocalDateTime {
        val today = from.toLocalDate()
        val todaysSlots = CHECK_HOURS.map { today.atTime(it, 0) }
        return todaysSlots.firstOrNull { it.isAfter(from) } ?: today.plusDays(1)
            .atTime(CHECK_HOURS.first(), 0)
    }
}
