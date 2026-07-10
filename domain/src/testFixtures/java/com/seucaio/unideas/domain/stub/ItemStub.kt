package com.seucaio.unideas.domain.stub

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Tag
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Shared [Item] samples for tests across modules.
 *
 * All dates are fixed (no `now()`) so tests are deterministic — use [TODAY]
 * as the reference "today" when deriving urgency.
 */
object ItemStub {

    val TODAY: LocalDate = LocalDate.of(2026, 7, 1)
    val CREATED_AT: LocalDateTime = TODAY.minusDays(7).atTime(10, 0)

    fun task(
        id: Long = 1L,
        title: String = "Pagar contas",
        description: String? = null,
        sectionId: Long? = null,
        dueDate: LocalDate? = TODAY.plusDays(2),
        recurrence: Recurrence = Recurrence.None,
        completedAt: LocalDateTime? = null,
        createdAt: LocalDateTime = CREATED_AT,
        tags: List<Tag> = emptyList(),
    ): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        description = description,
        sectionId = sectionId,
        dueDate = dueDate,
        recurrence = recurrence,
        completedAt = completedAt,
        createdAt = createdAt,
        tags = tags,
    )

    fun note(
        id: Long = 2L,
        title: String = "Ideia de projeto",
        description: String? = "Anotação livre",
        sectionId: Long? = null,
        createdAt: LocalDateTime = CREATED_AT,
        tags: List<Tag> = emptyList(),
    ): Item = Item(
        id = id,
        type = ItemType.NOTE,
        title = title,
        description = description,
        sectionId = sectionId,
        createdAt = createdAt,
        tags = tags,
    )

    fun overdueTask(id: Long = 3L): Item =
        task(id = id, title = "Tarefa vencida", dueDate = TODAY.minusDays(1))

    fun recurringTask(id: Long = 4L): Item =
        task(id = id, title = "Tarefa recorrente", recurrence = Recurrence.Weekly)

    fun completedTask(id: Long = 5L): Item =
        task(id = id, title = "Tarefa concluída", completedAt = TODAY.atTime(9, 30))
}
