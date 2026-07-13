package com.seucaio.unideas.feature.settings.viewmodel

import com.seucaio.unideas.domain.model.SeedScope

/** Which debug dialog (if any) is showing on top of the settings shell. */
sealed interface SettingsDialogState {

    data object None : SettingsDialogState

    /** Seed-scope bottom sheet; [selectedScope] is `null` until the user picks one. */
    data class SelectingSeedScope(val selectedScope: SeedScope? = null) : SettingsDialogState
}
