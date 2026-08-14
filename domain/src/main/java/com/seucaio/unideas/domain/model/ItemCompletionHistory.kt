package com.seucaio.unideas.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * One occurrence of a recurring [Item]'s series — recorded when [CompleteItemUseCase][
 * com.seucaio.unideas.domain.usecase.item.CompleteItemUseCase] advances the item's `dueDate`
 * (completed) or [ReminderCheckWorker] detects a scheduled date that passed unattended (missed).
 *
 * @property completedAt `null` means the occurrence was missed (recurrence advanced without the
 *   item ever being marked done for [scheduledDate]).
 * @property originalScheduledDate the `dueDate` this occurrence had before it was extended one or
 *   more times, if it was ever extended before resolving — `null` when it never was. Distinct
 *   from [scheduledDate], which is the (possibly extended) date it actually resolved against.
 * @property extensionCount how many times "extend deadline" pushed this occurrence forward before
 *   it resolved. `0` when it was never extended.
 */
data class ItemCompletionHistory(
    val id: Long = 0L,
    val itemId: Long,
    val scheduledDate: LocalDate,
    val completedAt: LocalDateTime?,
    val note: String? = null,
    val originalScheduledDate: LocalDate? = null,
    val extensionCount: Int = 0,
) {
    val status: CompletionStatus get() = when {
        completedAt == null -> CompletionStatus.MISSED
        completedAt.toLocalDate().isAfter(scheduledDate) -> CompletionStatus.LATE
        else -> CompletionStatus.ON_TIME
    }
}

enum class CompletionStatus {
    ON_TIME,
    LATE,
    MISSED,
}
