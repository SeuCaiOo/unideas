package com.seucaio.unideas.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.features.allpriorities.screen.AllPrioritiesScreen
import com.seucaio.unideas.feature.home.features.archiveditems.screen.ArchivedItemsScreen
import com.seucaio.unideas.feature.home.features.home.screen.HomeScreen

fun NavGraphBuilder.homeNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToDetailForLateCompletion: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onNavigateToArchivedItems: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBackupSettings: () -> Unit,
) {
    composable<HomeRoute.AllPriorities> {
        AllPrioritiesScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
    composable<HomeRoute.ArchivedItems> {
        ArchivedItemsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
    composable<HomeRoute.Home> { backStackEntry ->
        HomeScreen(
            savedStateHandle = backStackEntry.savedStateHandle,
            onNavigateBack = null,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToDetailForLateCompletion = onNavigateToDetailForLateCompletion,
            onNavigateToAddItem = onNavigateToAddItem,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
            onNavigateToArchivedItems = onNavigateToArchivedItems,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToBackupSettings = onNavigateToBackupSettings,
        )
    }
}
