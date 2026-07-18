package com.seucaio.unideas.feature.home.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.allpriorities.screen.AllPrioritiesScreen
import com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen
import com.seucaio.unideas.feature.home.features.panel.screen.HomeScreenV2

fun NavGraphBuilder.homeNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    composable<HomeRoute.Panel> {
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            HomeScreenV2(
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToForm = onNavigateToForm,
                onNavigateToAllPriorities = onNavigateToAllPriorities,
                onNavigateToSettings = onNavigateToSettings,
            )
        } else {
            HomeScreen(
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToForm = onNavigateToForm,
                onNavigateToAllPriorities = onNavigateToAllPriorities,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
    composable<HomeRoute.AllPriorities> {
        AllPrioritiesScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
}
