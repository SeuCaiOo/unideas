package com.seucaio.unideas.core.notifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seucaio.unideas.core.notifications.notification.ReminderNotifier
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ReminderTier
import com.seucaio.unideas.domain.usecase.item.GetItemsWithDueDateUseCase
import com.seucaio.unideas.domain.usecase.item.ProcessMissedOccurrencesUseCase
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

/**
 * Runs every [ReminderScheduler.CHECK_HOURS] slot (or on-demand via [ReminderScheduler.refreshNow]):
 * scans every item with a due date, advances any recurring item past occurrences it missed
 * ([ProcessMissedOccurrencesUseCase], #126), buckets each by [ReminderTier.of], and refreshes the
 * two aggregated notifications. Radar tier needs no notification — already surfaced by the
 * priority panel.
 */
class ReminderCheckWorker(
    context: Context,
    params: WorkerParameters,
    private val getItemsWithDueDate: GetItemsWithDueDateUseCase,
    private val processMissedOccurrences: ProcessMissedOccurrencesUseCase,
    private val notifier: ReminderNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = LocalDateTime.now()
        val nextCheck = ReminderScheduler.nextCheckSlot(now)
        val items = getItemsWithDueDate().first()
            .map { processMissedOccurrences(it, now.toLocalDate()).getOrDefault(it) }

        val normal = mutableListOf<Item>()
        val urgent = mutableListOf<Item>()
        for (item in items) {
            when (ReminderTier.of(item, now, nextCheck)) {
                ReminderTier.NORMAL -> normal += item
                ReminderTier.URGENT -> urgent += item
                ReminderTier.NOT_YET -> Unit
            }
        }

        val silent = inputData.getBoolean(ReminderScheduler.KEY_SILENT, false)
        notifier.notify(normal = normal, urgent = urgent, silent = silent)
        return Result.success()
    }
}
