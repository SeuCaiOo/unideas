package com.seucaio.unideas.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Core domain model: a task or a note.
 *
 * Dates are `java.time` types — epoch millis stay in the persistence layer.
 *
 * @property recurrence only valid when [dueDate] is not null.
 * @property completedAt non-null means completed (only meaningful for [ItemType.TASK]).
 */
data class Item(
    val id: Long = 0L,
    val type: ItemType,
    val title: String,
    val description: String? = null,
    val sectionId: Long? = null,
    val dueDate: LocalDate? = null,
    val recurrence: Recurrence = Recurrence.None,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val tags: List<Tag> = emptyList(),
) {
    val isCompleted: Boolean get() = completedAt != null

    val isRecurring: Boolean get() = recurrence !is Recurrence.None

    /**
     * Urgency derived from [dueDate] vs [today]; see [UrgencyLevel.of].
     */
    fun urgency(today: LocalDate, dueSoonDays: Int): UrgencyLevel =
        UrgencyLevel.of(dueDate, today, dueSoonDays)
}
