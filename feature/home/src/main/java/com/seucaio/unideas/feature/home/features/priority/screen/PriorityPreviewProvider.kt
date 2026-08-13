package com.seucaio.unideas.feature.home.features.priority.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityItemsState
import java.time.LocalDate
import java.time.LocalDateTime

/** Bundles the two `StateFlow`s a "Success" preview needs together. */
internal data class PriorityPreviewFixture(
    val itemsState: PriorityItemsState,
    val hasAnyItem: Boolean,
)

internal class PriorityPreviewProvider : PreviewParameterProvider<PriorityPreviewFixture> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)

    private fun task(id: Long, title: String, dueDate: LocalDate?): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        dueDate = dueDate,
        recurrence = Recurrence.None,
        createdAt = createdAt,
    )

    override val values: Sequence<PriorityPreviewFixture> = sequenceOf(
        // under the panel limit: no "see all" button
        PriorityPreviewFixture(
            itemsState = PriorityItemsState(
                priorityItems = listOf(task(1L, "Pagar contas", LocalDate.of(2026, 6, 25))),
                showSeeAllButton = false,
            ),
            hasAnyItem = true,
        ),
        // over the panel limit: capped list + "see all" button
        PriorityPreviewFixture(
            itemsState = PriorityItemsState(
                priorityItems = (1..5).map { task(it.toLong(), "Prioridade $it", LocalDate.of(2026, 6, 20)) },
                showSeeAllButton = true,
            ),
            hasAnyItem = true,
        ),
        // empty priority panel, but user has items elsewhere
        PriorityPreviewFixture(
            itemsState = PriorityItemsState(),
            hasAnyItem = true,
        ),
        // true first-run empty state: no items anywhere in the app
        PriorityPreviewFixture(
            itemsState = PriorityItemsState(),
            hasAnyItem = false,
        ),
    )
}
