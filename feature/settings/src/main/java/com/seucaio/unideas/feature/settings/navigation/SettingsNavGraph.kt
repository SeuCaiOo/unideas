package com.seucaio.unideas.feature.settings.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.feature.settings.SettingsScreen
import com.seucaio.unideas.feature.settings.SettingsScreenV2

fun NavGraphBuilder.settingsNavGraph(
    config: SettingsScreenConfig,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSections: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToItems: () -> Unit,
) {
    composable<SettingsRoute.Settings> {
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            SettingsScreenV2(
                versionName = config.versionName,
                showDebugSection = config.showDebugSection,
                onNavigateBack = onNavigateBack,
                onNavigateToSections = onNavigateToSections,
                onNavigateToTags = onNavigateToTags,
                onNavigateToItems = onNavigateToItems,
            )
        } else {
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
}
