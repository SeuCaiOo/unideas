package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.domain.model.Section

/** User interactions on the manage-sections screen. */
sealed interface SectionsEvent {

    data class OnAddClicked(val name: String) : SectionsEvent

    data class OnRenameClicked(val section: Section, val newName: String) : SectionsEvent

    data class OnDeleteClicked(val id: Long) : SectionsEvent
}
