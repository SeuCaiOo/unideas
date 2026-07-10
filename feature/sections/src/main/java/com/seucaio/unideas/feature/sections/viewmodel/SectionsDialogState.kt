package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.domain.model.Section

/** Which entity dialog (if any) is showing on top of the sections list. */
sealed interface SectionsDialogState {

    data object None : SectionsDialogState

    data object Add : SectionsDialogState

    data class Rename(val section: Section) : SectionsDialogState

    data class Delete(val section: Section) : SectionsDialogState
}
