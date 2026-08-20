package com.seucaio.unideas.feature.items.ui.screens.history

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryDialogState
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryUiState
import java.time.LocalDate
import java.time.LocalDateTime

data class ItemHistoryPreviewScenario(
    val uiState: ItemHistoryUiState,
    val dialogState: ItemHistoryDialogState = ItemHistoryDialogState.None,
)

class ItemHistoryScreenContentPreviewProvider : PreviewParameterProvider<ItemHistoryPreviewScenario> {

    private val filledState = ItemHistoryUiState(
        history = listOf(
            ItemCompletionHistory(
                id = 1L,
                itemId = 1L,
                scheduledDate = LocalDate.of(2026, 8, 14),
                completedAt = LocalDateTime.of(2026, 8, 14, 9, 12),
            ),
            ItemCompletionHistory(
                id = 2L,
                itemId = 1L,
                scheduledDate = LocalDate.of(2026, 8, 1),
                completedAt = LocalDateTime.of(2026, 8, 8, 20, 47),
                note = "Sem tempo no dia, terminei no fim de semana",
                originalScheduledDate = LocalDate.of(2026, 7, 25),
                extensionCount = 2,
            ),
            ItemCompletionHistory(
                id = 3L,
                itemId = 1L,
                scheduledDate = LocalDate.of(2026, 7, 24),
                completedAt = null,
                note = "Não deu essa semana",
            ),
        ),
    )

    // Real-world common case: several auto-generated missed occurrences in a row, none with a
    // note — this is what exposed the wasted-space layout bug (each card only ever showed a
    // date + status line, but still reserved room for details that never existed).
    private val sparseMissedState = ItemHistoryUiState(
        history = listOf(
            ItemCompletionHistory(id = 10L, itemId = 1L, scheduledDate = LocalDate.of(2026, 8, 18), completedAt = null),
            ItemCompletionHistory(id = 11L, itemId = 1L, scheduledDate = LocalDate.of(2026, 8, 17), completedAt = null),
            ItemCompletionHistory(id = 12L, itemId = 1L, scheduledDate = LocalDate.of(2026, 8, 16), completedAt = null),
        ),
    )

    override val values: Sequence<ItemHistoryPreviewScenario> = sequenceOf(
        ItemHistoryPreviewScenario(filledState),
        ItemHistoryPreviewScenario(sparseMissedState),
        ItemHistoryPreviewScenario(ItemHistoryUiState()),
        ItemHistoryPreviewScenario(
            uiState = filledState,
            dialogState = ItemHistoryDialogState.AddEditEntry(existing = filledState.history.first()),
        ),
        ItemHistoryPreviewScenario(
            uiState = filledState,
            dialogState = ItemHistoryDialogState.DeleteConfirm(entry = filledState.history.first()),
        ),
    )
}

class ItemHistoryCardPreviewProvider : PreviewParameterProvider<ItemCompletionHistory> {

    override val values: Sequence<ItemCompletionHistory> = sequenceOf(
        ItemCompletionHistory(
            id = 1L,
            itemId = 1L,
            scheduledDate = LocalDate.of(2026, 8, 14),
            completedAt = LocalDateTime.of(2026, 8, 14, 9, 12),
        ),
        ItemCompletionHistory(
            id = 2L,
            itemId = 1L,
            scheduledDate = LocalDate.of(2026, 8, 1),
            completedAt = LocalDateTime.of(2026, 8, 8, 20, 47),
            note = "Sem tempo no dia, terminei no fim de semana",
            originalScheduledDate = LocalDate.of(2026, 7, 25),
            extensionCount = 2,
        ),
        ItemCompletionHistory(
            id = 3L,
            itemId = 1L,
            scheduledDate = LocalDate.of(2026, 7, 24),
            completedAt = null,
            note = "Não deu essa semana",
        ),
        ItemCompletionHistory(
            id = 4L,
            itemId = 1L,
            scheduledDate = LocalDate.of(2026, 8, 18),
            completedAt = null,
        ),
        ItemCompletionHistory(
            id = 5L,
            itemId = 1L,
            scheduledDate = LocalDate.of(2026, 8, 17),
            completedAt = LocalDateTime.of(2026, 8, 17, 9, 0),
        ),
    )
}
