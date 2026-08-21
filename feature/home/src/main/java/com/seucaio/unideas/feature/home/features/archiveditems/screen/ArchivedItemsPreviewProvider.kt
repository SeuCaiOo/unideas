package com.seucaio.unideas.feature.home.features.archiveditems.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.archiveditems.viewmodel.ArchivedItemsUiState
import java.time.LocalDateTime

class ArchivedItemsPreviewProvider : PreviewParameterProvider<ArchivedItemsUiState> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)

    private fun task(id: Long, title: String): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        createdAt = createdAt,
        status = ItemStatus.ARCHIVED,
    )

    override val values: Sequence<ArchivedItemsUiState> = sequenceOf(
        ArchivedItemsUiState.Loading,
        ArchivedItemsUiState.Success(
            items = listOf(
                task(1L, "Reforma antiga"),
                task(2L, "Projeto pausado"),
            ),
        ),
        ArchivedItemsUiState.Success(items = emptyList()),
        ArchivedItemsUiState.Error(R.string.archived_items_load_error),
    )
}
