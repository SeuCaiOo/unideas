package com.seucaio.unideas.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsNavGraph(
    versionName: String,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
) {
    composable<SettingsRoute.Settings> {
        SettingsScreen(
            versionName = versionName,
            onNavigateBack = onNavigateBack,
            onNavigateToSections = onNavigateToSections,
            onNavigateToTags = onNavigateToTags,
        )
    }
}
