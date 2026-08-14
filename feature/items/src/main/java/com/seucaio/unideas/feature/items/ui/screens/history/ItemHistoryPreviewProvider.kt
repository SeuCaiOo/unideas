package com.seucaio.unideas.feature.items.ui.screens.history

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.ItemCompletionHistory
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryUiState
import java.time.LocalDate
import java.time.LocalDateTime

class ItemHistoryContentPreviewProvider : PreviewParameterProvider<ItemHistoryUiState> {

    override val values: Sequence<ItemHistoryUiState> = sequenceOf(
        ItemHistoryUiState(
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
        ),
        ItemHistoryUiState(),
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
    )
}
