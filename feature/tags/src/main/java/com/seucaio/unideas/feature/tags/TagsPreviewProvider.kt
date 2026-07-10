package com.seucaio.unideas.feature.tags

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.tags.viewmodel.TagsDialogState
import com.seucaio.unideas.feature.tags.viewmodel.TagsUiState

class TagsPreviewProvider : PreviewParameterProvider<TagsUiState> {

    override val values: Sequence<TagsUiState> = sequenceOf(
        TagsUiState.Loading,
        TagsUiState.Success(tags = emptyList()),
        TagsUiState.Success(
            tags = listOf(
                Tag(id = 1L, name = "urgente"),
                Tag(id = 2L, name = "pessoal"),
            ),
        ),
        TagsUiState.Error(messageRes = R.string.tags_load_error),
        TagsUiState.Success(
            tags = listOf(Tag(id = 1L, name = "urgente")),
            dialog = TagsDialogState.Add,
        ),
        TagsUiState.Success(
            tags = listOf(Tag(id = 1L, name = "urgente")),
            dialog = TagsDialogState.Rename(Tag(id = 1L, name = "urgente")),
        ),
        TagsUiState.Success(
            tags = listOf(Tag(id = 1L, name = "urgente")),
            dialog = TagsDialogState.Delete(Tag(id = 1L, name = "urgente")),
        ),
    )
}
