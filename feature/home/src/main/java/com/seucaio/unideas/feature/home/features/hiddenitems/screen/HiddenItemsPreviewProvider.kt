package com.seucaio.unideas.feature.home.features.hiddenitems.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel.HiddenItemsUiState
import java.time.LocalDateTime

class HiddenItemsPreviewProvider : PreviewParameterProvider<HiddenItemsUiState> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)

    private fun task(id: Long, title: String): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        createdAt = createdAt,
        isConfidential = true,
    )

    override val values: Sequence<HiddenItemsUiState> = sequenceOf(
        HiddenItemsUiState.Loading,
        HiddenItemsUiState.Success(
            items = listOf(
                task(1L, "Senha do cofre"),
                task(2L, "Dados sigilosos"),
            ),
        ),
        HiddenItemsUiState.Success(items = emptyList()),
        HiddenItemsUiState.Error(R.string.hidden_items_load_error),
    )
}
