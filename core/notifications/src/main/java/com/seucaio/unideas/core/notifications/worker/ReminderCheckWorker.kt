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
 *
 * [GetItemsWithDueDateUseCase]'s query can't exclude a recurring item completed for its *current*
 * cycle (it never sets `completedAt`, only `lastCompletedScheduledDate`) without also hiding it
 * from [ProcessMissedOccurrencesUseCase] once that cycle actually goes overdue — so the
 * already-resolved filter has to run here instead, after missed-occurrence processing.
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
            .filterNot { it.isCompleted }

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
