package com.seucaio.unideas.feature.settings.navigation

import kotlinx.serialization.Serializable

sealed interface SettingsRoute {

    @Serializable
    data class Settings(val openBackupSheet: Boolean = false) : SettingsRoute
}
