package com.seucaio.unideas.feature.tags

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.feature.tags.viewmodel.TagsDialogState
import com.seucaio.unideas.feature.tags.viewmodel.TagsUiState

/** [uiState] and [dialogState] are independent StateFlows in the ViewModel — bundled here only so
 * a single PreviewParameterProvider can simulate every combination for the Screen preview. */
data class TagsPreviewState(
    val uiState: TagsUiState,
    val dialogState: TagsDialogState = TagsDialogState.None,
)

class TagsPreviewProvider : PreviewParameterProvider<TagsPreviewState> {

    override val values: Sequence<TagsPreviewState> = sequenceOf(
        TagsPreviewState(TagsUiState.Loading),
        TagsPreviewState(TagsUiState.Success(tags = emptyList())),
        TagsPreviewState(
            TagsUiState.Success(
                tags = listOf(
                    Tag(id = 1L, name = "urgente"),
                    Tag(id = 2L, name = "pessoal"),
                ),
            ),
        ),
        TagsPreviewState(TagsUiState.Error(messageRes = R.string.tags_load_error)),
        TagsPreviewState(
            uiState = TagsUiState.Success(tags = listOf(Tag(id = 1L, name = "urgente"))),
            dialogState = TagsDialogState.Add,
        ),
        TagsPreviewState(
            uiState = TagsUiState.Success(tags = listOf(Tag(id = 1L, name = "urgente"))),
            dialogState = TagsDialogState.Rename(Tag(id = 1L, name = "urgente")),
        ),
        TagsPreviewState(
            uiState = TagsUiState.Success(tags = listOf(Tag(id = 1L, name = "urgente"))),
            dialogState = TagsDialogState.Delete(Tag(id = 1L, name = "urgente")),
        ),
    )
}
