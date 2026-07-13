package com.seucaio.unideas.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsNavGraph(
    config: SettingsScreenConfig,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToItems: () -> Unit,
) {
    composable<SettingsRoute.Settings> {
        SettingsScreen(
            versionName = config.versionName,
            showDebugSection = config.showDebugSection,
            onNavigateBack = onNavigateBack,
            onNavigateToSections = onNavigateToSections,
            onNavigateToTags = onNavigateToTags,
            onNavigateToItems = onNavigateToItems,
        )
    }
}
