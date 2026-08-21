package com.seucaio.unideas.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Core domain model: a task or a note.
 *
 * Dates are `java.time` types — epoch millis stay in the persistence layer.
 *
 * @property recurrence only valid when [dueDate] is not null.
 * @property dueTime optional; only valid when [dueDate] is not null. Does not replace [dueDate]
 *   with a `LocalDateTime` on purpose — [UrgencyLevel] and existing screens stay untouched.
 * @property reminderWarning only valid when [dueDate] is not null. See [ReminderTier.of].
 * @property completedAt non-null means completed (only meaningful for [ItemType.TASK]).
 * @property lastCompletedScheduledDate the `dueDate` of a recurring task's most recently
 *   completed occurrence — only meaningful when [isRecurring]; see [isCompleted].
 * @property isPinned when true, appears in the priority panel regardless of [urgency].
 * @property status [ItemStatus.ARCHIVED] hides the item from the normal listing and pauses its
 *   reminders/occurrence generation (see [ItemStatus]).
 * @property pendingExtensionOriginalDueDate the `dueDate` this occurrence had *before* the first
 *   "extend deadline" action, if any — cleared once the occurrence resolves (completed/ignored).
 *   `null` means this occurrence has never been extended since its last resolution.
 * @property pendingExtensionCount how many times this occurrence's `dueDate` was pushed forward
 *   since its last resolution — carried into [ItemCompletionHistory] when it finally resolves.
 */
data class Item(
    val id: Long = 0L,
    val type: ItemType,
    val title: String,
    val description: String? = null,
    val sectionId: Long? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val recurrence: Recurrence = Recurrence.None,
    val reminderWarning: ReminderWarning = ReminderWarning.None,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val lastCompletedScheduledDate: LocalDate? = null,
    val isPinned: Boolean = false,
    val pendingExtensionOriginalDueDate: LocalDate? = null,
    val pendingExtensionCount: Int = 0,
    val status: ItemStatus = ItemStatus.ACTIVE,
    val tags: List<Tag> = emptyList(),
) {
    /**
     * A recurring task's `dueDate` doesn't move when completed (it only advances once its
     * occurrence is actually in the past — [com.seucaio.unideas.domain.usecase.item.ProcessMissedOccurrencesUseCase]),
     * so completion is tracked separately via [lastCompletedScheduledDate] instead of [completedAt].
     */
    val isCompleted: Boolean
        get() = if (isRecurring && dueDate != null) lastCompletedScheduledDate == dueDate else completedAt != null

    val isRecurring: Boolean get() = recurrence !is Recurrence.None

    /**
     * Urgency derived from [dueDate] vs [today]; see [UrgencyLevel.of].
     */
    fun urgency(today: LocalDate, dueSoonDays: Int): UrgencyLevel =
        UrgencyLevel.of(dueDate, today, dueSoonDays)
}
