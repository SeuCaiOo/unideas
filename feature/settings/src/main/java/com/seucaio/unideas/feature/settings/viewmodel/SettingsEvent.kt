package com.seucaio.unideas.feature.settings.viewmodel

/** User interactions on the settings shell. */
sealed interface SettingsEvent {

    data object OnOrganizeSectionsClicked : SettingsEvent

    data object OnOrganizeTagsClicked : SettingsEvent

    data object OnItemsClicked : SettingsEvent
}
