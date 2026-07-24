package com.seucaio.unideas.feature.items.ui.screens.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.screens.list.viewmodel.ItemsListUiState
import java.time.LocalDateTime

class ItemsListPreviewProvider : PreviewParameterProvider<ItemsListUiState> {

    override val values: Sequence<ItemsListUiState> = sequenceOf(
        ItemsListUiState.Loading,
        ItemsListUiState.Success(items = emptyList()),
        ItemsListUiState.Success(
            items = listOf(
                Item(
                    id = 1L,
                    type = ItemType.TASK,
                    title = "Pagar contas",
                    createdAt = LocalDateTime.of(2026, 7, 10, 9, 0),
                ),
                Item(
                    id = 2L,
                    type = ItemType.NOTE,
                    title = "Ideia de projeto",
                    createdAt = LocalDateTime.of(2026, 7, 9, 14, 30),
                ),
            ),
        ),
        ItemsListUiState.Error(messageRes = R.string.items_list_load_error),
    )
}
