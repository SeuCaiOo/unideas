package com.seucaio.unideas.feature.sections

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.core.common.crud.EntityCrudUiState
import com.seucaio.unideas.core.common.crud.EntityDialogState
import com.seucaio.unideas.domain.model.Section

/** [uiState] and [dialogState] are independent StateFlows in the ViewModel — bundled here only so
 * a single PreviewParameterProvider can simulate every combination for the Screen preview. */
data class SectionsPreviewState(
    val uiState: EntityCrudUiState<Section>,
    val dialogState: EntityDialogState<Section> = EntityDialogState.None,
)

class SectionsPreviewProvider : PreviewParameterProvider<SectionsPreviewState> {

    override val values: Sequence<SectionsPreviewState> = sequenceOf(
        SectionsPreviewState(EntityCrudUiState.Loading),
        SectionsPreviewState(EntityCrudUiState.Success(items = emptyList())),
        SectionsPreviewState(
            EntityCrudUiState.Success(
                items = listOf(
                    Section(id = 1L, name = "Work"),
                    Section(id = 2L, name = "Home"),
                ),
            ),
        ),
        SectionsPreviewState(EntityCrudUiState.Error(messageRes = R.string.sections_load_error)),
        SectionsPreviewState(
            uiState = EntityCrudUiState.Success(items = listOf(Section(id = 1L, name = "Work"))),
            dialogState = EntityDialogState.Add,
        ),
        SectionsPreviewState(
            uiState = EntityCrudUiState.Success(items = listOf(Section(id = 1L, name = "Work"))),
            dialogState = EntityDialogState.Rename(Section(id = 1L, name = "Work")),
        ),
        SectionsPreviewState(
            uiState = EntityCrudUiState.Success(items = listOf(Section(id = 1L, name = "Work"))),
            dialogState = EntityDialogState.Delete(Section(id = 1L, name = "Work")),
        ),
    )
}
