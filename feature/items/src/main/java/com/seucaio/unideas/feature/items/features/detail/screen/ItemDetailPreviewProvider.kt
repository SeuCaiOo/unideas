package com.seucaio.unideas.feature.items.features.detail.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailUiState
import java.time.LocalDate
import java.time.LocalDateTime

class ItemDetailPreviewProvider : PreviewParameterProvider<ItemDetailUiState> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)

    override val values: Sequence<ItemDetailUiState> = sequenceOf(
        ItemDetailUiState.Loading,
        ItemDetailUiState.Success(
            item = Item(
                id = 1L,
                type = ItemType.TASK,
                title = "Pagar contas",
                description = "Conta de luz e água",
                sectionId = 1L,
                dueDate = LocalDate.of(2026, 7, 5),
                recurrence = Recurrence.Weekly,
                createdAt = createdAt,
                tags = listOf(Tag(id = 1L, name = "urgente")),
            ),
            sectionName = "Trabalho",
        ),
        ItemDetailUiState.Success(
            item = Item(
                id = 2L,
                type = ItemType.TASK,
                title = "Tarefa concluída",
                dueDate = LocalDate.of(2026, 6, 25),
                completedAt = LocalDateTime.of(2026, 6, 24, 9, 0),
                createdAt = createdAt,
            ),
        ),
        ItemDetailUiState.Success(
            item = Item(
                id = 3L,
                type = ItemType.NOTE,
                title = "Ideia de projeto",
                description = "Anotação livre sobre o app",
                createdAt = createdAt,
                tags = listOf(Tag(id = 2L, name = "pessoal")),
            ),
        ),
        ItemDetailUiState.Error(messageRes = R.string.item_detail_load_error),
    )
}
