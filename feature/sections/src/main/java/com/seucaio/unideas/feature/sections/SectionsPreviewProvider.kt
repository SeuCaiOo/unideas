package com.seucaio.unideas.feature.sections

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.feature.sections.viewmodel.SectionsDialogState
import com.seucaio.unideas.feature.sections.viewmodel.SectionsUiState

class SectionsPreviewProvider : PreviewParameterProvider<SectionsUiState> {

    override val values: Sequence<SectionsUiState> = sequenceOf(
        SectionsUiState.Loading,
        SectionsUiState.Success(sections = emptyList()),
        SectionsUiState.Success(
            sections = listOf(
                Section(id = 1L, name = "Work"),
                Section(id = 2L, name = "Home"),
            ),
        ),
        SectionsUiState.Error(messageRes = R.string.sections_load_error),
        SectionsUiState.Success(
            sections = listOf(Section(id = 1L, name = "Work")),
            dialog = SectionsDialogState.Add,
        ),
        SectionsUiState.Success(
            sections = listOf(Section(id = 1L, name = "Work")),
            dialog = SectionsDialogState.Rename(Section(id = 1L, name = "Work")),
        ),
        SectionsUiState.Success(
            sections = listOf(Section(id = 1L, name = "Work")),
            dialog = SectionsDialogState.Delete(Section(id = 1L, name = "Work")),
        ),
    )
}
