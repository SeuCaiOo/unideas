package com.seucaio.unideas.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.seucaio.unideas.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsNavGraph(
    config: SettingsScreenConfig,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToItems: () -> Unit,
    onLogoutComplete: () -> Unit,
) {
    composable<SettingsRoute.Settings> { backStackEntry ->
        val route = backStackEntry.toRoute<SettingsRoute.Settings>()
        SettingsScreen(
            versionName = config.versionName,
            showDebugSection = config.showDebugSection,
            openBackupSheet = route.openBackupSheet,
            onNavigateBack = onNavigateBack,
            onNavigateToSections = onNavigateToSections,
            onNavigateToTags = onNavigateToTags,
            onNavigateToItems = onNavigateToItems,
            onLogoutComplete = onLogoutComplete,
        )
    }
}
