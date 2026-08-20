package com.seucaio.unideas.feature.items.ui.screens.config.sectionstags.viewmodel

sealed interface SectionsTagsEvent {
    data class OnSectionCreateRequested(val name: String) : SectionsTagsEvent
    data class OnTagCreateRequested(val name: String) : SectionsTagsEvent
}
