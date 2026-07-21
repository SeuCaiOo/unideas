package com.seucaio.unideas.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.allpriorities.screen.AllPrioritiesScreen
import com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen
import com.seucaio.unideas.feature.home.features.panel.screen.HomeScreen

fun NavGraphBuilder.homeNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBrowse: () -> Unit,
) {
    composable<HomeRoute.Panel> {
        HomeScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToForm = onNavigateToForm,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToBrowse = onNavigateToBrowse,
        )
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
