package com.seucaio.unideas.feature.items.ui.screens.config.sectionstags.viewmodel

sealed interface SectionsTagsEvent {
    data object OnAddSectionClicked : SectionsTagsEvent
    data object OnAddTagClicked : SectionsTagsEvent
    data object OnDialogDismissed : SectionsTagsEvent
    data class OnSectionCreateRequested(val name: String) : SectionsTagsEvent
    data class OnTagCreateRequested(val name: String) : SectionsTagsEvent
}
