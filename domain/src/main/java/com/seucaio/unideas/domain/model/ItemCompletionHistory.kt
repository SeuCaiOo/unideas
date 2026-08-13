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
 */
data class ItemCompletionHistory(
    val id: Long = 0L,
    val itemId: Long,
    val scheduledDate: LocalDate,
    val completedAt: LocalDateTime?,
    val note: String? = null,
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
