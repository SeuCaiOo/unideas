package com.seucaio.unideas.feature.tags

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.core.common.crud.EntityCrudUiState
import com.seucaio.unideas.core.common.crud.EntityDialogState
import com.seucaio.unideas.domain.model.Tag

/** [uiState] and [dialogState] are independent StateFlows in the ViewModel — bundled here only so
 * a single PreviewParameterProvider can simulate every combination for the Screen preview. */
data class TagsPreviewState(
    val uiState: EntityCrudUiState<Tag>,
    val dialogState: EntityDialogState<Tag> = EntityDialogState.None,
)

class TagsPreviewProvider : PreviewParameterProvider<TagsPreviewState> {

    override val values: Sequence<TagsPreviewState> = sequenceOf(
        TagsPreviewState(EntityCrudUiState.Loading),
        TagsPreviewState(EntityCrudUiState.Success(items = emptyList())),
        TagsPreviewState(
            EntityCrudUiState.Success(
                items = listOf(
                    Tag(id = 1L, name = "urgente"),
                    Tag(id = 2L, name = "pessoal"),
                ),
            ),
        ),
        TagsPreviewState(EntityCrudUiState.Error(messageRes = R.string.tags_load_error)),
        TagsPreviewState(
            uiState = EntityCrudUiState.Success(items = listOf(Tag(id = 1L, name = "urgente"))),
            dialogState = EntityDialogState.Add,
        ),
        TagsPreviewState(
            uiState = EntityCrudUiState.Success(items = listOf(Tag(id = 1L, name = "urgente"))),
            dialogState = EntityDialogState.Rename(Tag(id = 1L, name = "urgente")),
        ),
        TagsPreviewState(
            uiState = EntityCrudUiState.Success(items = listOf(Tag(id = 1L, name = "urgente"))),
            dialogState = EntityDialogState.Delete(Tag(id = 1L, name = "urgente")),
        ),
    )
}
