package com.seucaio.unideas.feature.home.features.panel.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeItemsState
import java.time.LocalDate
import java.time.LocalDateTime

/** Bundles the two `StateFlow`s a "Success" preview needs together. */
internal data class HomePreviewFixture(
    val itemsState: HomeItemsState,
    val hasAnyItem: Boolean,
)

internal class HomePreviewProvider : PreviewParameterProvider<HomePreviewFixture> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)

    private fun task(id: Long, title: String, dueDate: LocalDate?): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        dueDate = dueDate,
        recurrence = Recurrence.None,
        createdAt = createdAt,
    )

    override val values: Sequence<HomePreviewFixture> = sequenceOf(
        // under the panel limit: no "see all" button
        HomePreviewFixture(
            itemsState = HomeItemsState(
                priorityItems = listOf(task(1L, "Pagar contas", LocalDate.of(2026, 6, 25))),
                showSeeAllButton = false,
            ),
            hasAnyItem = true,
        ),
        // over the panel limit: capped list + "see all" button
        HomePreviewFixture(
            itemsState = HomeItemsState(
                priorityItems = (1..5).map { task(it.toLong(), "Prioridade $it", LocalDate.of(2026, 6, 20)) },
                showSeeAllButton = true,
            ),
            hasAnyItem = true,
        ),
        // empty priority panel, but user has items elsewhere
        HomePreviewFixture(
            itemsState = HomeItemsState(),
            hasAnyItem = true,
        ),
        // true first-run empty state: no items anywhere in the app
        HomePreviewFixture(
            itemsState = HomeItemsState(),
            hasAnyItem = false,
        ),
    )
}
