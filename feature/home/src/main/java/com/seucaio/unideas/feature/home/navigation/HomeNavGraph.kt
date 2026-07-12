package com.seucaio.unideas.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen

fun NavGraphBuilder.homeNavGraph(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    composable<HomeRoute.Panel> {
        HomeScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToForm = onNavigateToForm,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}
