package com.seucaio.unideas.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.allpriorities.screen.AllPrioritiesScreen
import com.seucaio.unideas.feature.home.features.home.screen.HomeScreen

fun NavGraphBuilder.homeNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    composable<HomeRoute.AllPriorities> {
        AllPrioritiesScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
    composable<HomeRoute.Home> {
        HomeScreen(
            onNavigateBack = null,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAddItem = onNavigateToAddItem,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}
