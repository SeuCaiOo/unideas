package com.seucaio.unideas.feature.home.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.allpriorities.screen.AllPrioritiesScreen
import com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen
import com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen
import com.seucaio.unideas.feature.home.features.panel.screen.HomeScreenV2

fun NavGraphBuilder.homeNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBrowse: () -> Unit,
) {
    composable<HomeRoute.Panel> {
        // #86 Pacote 1 POC (2026-07-21) — same route/place as always, driven by the existing
        // Settings > Debug > "Use V2 screens" toggle, not a separate destination.
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            HomeScreenV2(
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToForm = onNavigateToForm,
                onNavigateToAllPriorities = onNavigateToAllPriorities,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToBrowse = onNavigateToBrowse,
            )
        } else {
            HomeScreen(
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToForm = onNavigateToForm,
                onNavigateToAllPriorities = onNavigateToAllPriorities,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToBrowse = onNavigateToBrowse,
            )
        }
    }
    composable<HomeRoute.AllPriorities> {
        AllPrioritiesScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
    composable<HomeRoute.Browse> {
        BrowseScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToForm = onNavigateToForm,
        )
    }
}
