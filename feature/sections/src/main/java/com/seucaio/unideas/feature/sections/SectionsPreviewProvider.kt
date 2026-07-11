package com.seucaio.unideas.feature.sections

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.feature.sections.viewmodel.SectionsDialogState
import com.seucaio.unideas.feature.sections.viewmodel.SectionsUiState

/** [uiState] and [dialogState] are independent StateFlows in the ViewModel — bundled here only so
 * a single PreviewParameterProvider can simulate every combination for the Screen preview. */
data class SectionsPreviewState(
    val uiState: SectionsUiState,
    val dialogState: SectionsDialogState = SectionsDialogState.None,
)

class SectionsPreviewProvider : PreviewParameterProvider<SectionsPreviewState> {

    override val values: Sequence<SectionsPreviewState> = sequenceOf(
        SectionsPreviewState(SectionsUiState.Loading),
        SectionsPreviewState(SectionsUiState.Success(sections = emptyList())),
        SectionsPreviewState(
            SectionsUiState.Success(
                sections = listOf(
                    Section(id = 1L, name = "Work"),
                    Section(id = 2L, name = "Home"),
                ),
            ),
        ),
        SectionsPreviewState(SectionsUiState.Error(messageRes = R.string.sections_load_error)),
        SectionsPreviewState(
            uiState = SectionsUiState.Success(sections = listOf(Section(id = 1L, name = "Work"))),
            dialogState = SectionsDialogState.Add,
        ),
        SectionsPreviewState(
            uiState = SectionsUiState.Success(sections = listOf(Section(id = 1L, name = "Work"))),
            dialogState = SectionsDialogState.Rename(Section(id = 1L, name = "Work")),
        ),
        SectionsPreviewState(
            uiState = SectionsUiState.Success(sections = listOf(Section(id = 1L, name = "Work"))),
            dialogState = SectionsDialogState.Delete(Section(id = 1L, name = "Work")),
        ),
    )
}
