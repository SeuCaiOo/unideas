package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.sectionstags

sealed interface SectionsTagsDialogState {
    data object None : SectionsTagsDialogState
    data object AddSection : SectionsTagsDialogState
    data object AddTag : SectionsTagsDialogState
}
