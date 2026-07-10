package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.domain.model.Section

/** User interactions on the manage-sections screen. */
sealed interface SectionsEvent {

    data object OnAddClicked : SectionsEvent

    data class OnAddConfirmClicked(val name: String) : SectionsEvent

    data class OnRenameClicked(val section: Section) : SectionsEvent

    data class OnRenameConfirmClicked(val newName: String) : SectionsEvent

    data class OnDeleteClicked(val section: Section) : SectionsEvent

    data object OnDeleteConfirmClicked : SectionsEvent

    data object OnDialogDismissed : SectionsEvent

    data object OnRetryClicked : SectionsEvent
}
