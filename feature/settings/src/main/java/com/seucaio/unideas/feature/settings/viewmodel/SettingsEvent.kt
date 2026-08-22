package com.seucaio.unideas.feature.settings.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.domain.model.SeedScope

/** User interactions on the settings shell. */
sealed interface SettingsEvent {

    data object OnOrganizeSectionsClicked : SettingsEvent

    data object OnOrganizeTagsClicked : SettingsEvent

    data object OnItemsClicked : SettingsEvent

    /** Opens the seed-scope bottom sheet. */
    data object OnSeedDatabaseClicked : SettingsEvent

    data class OnSeedScopeSelected(val scope: SeedScope) : SettingsEvent

    data object OnSeedConfirmClicked : SettingsEvent

    data object OnSeedDialogDismissed : SettingsEvent

    data object OnClearDatabaseClicked : SettingsEvent

    data class OnLogoutConfirmed(val account: GoogleSignInAccount) : SettingsEvent

    data object OnAccountSignedOut : SettingsEvent
}
