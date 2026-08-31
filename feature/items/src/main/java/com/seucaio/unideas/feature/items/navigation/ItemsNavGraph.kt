package com.seucaio.unideas.feature.items.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.ui.screens.config.ItemConfigScreen
import com.seucaio.unideas.feature.items.ui.screens.detail.ItemDetailScreen
import com.seucaio.unideas.feature.items.ui.screens.history.ItemHistoryScreen
import com.seucaio.unideas.feature.items.ui.screens.list.ItemsListScreen

fun NavGraphBuilder.itemsNavGraph(
    onNavigateBack: ((Boolean) -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    onNavigateToConfig: (Long, Boolean) -> Unit,
) {
    composable<ItemsRoute.Detail>(
        deepLinks = listOf(navDeepLink<ItemsRoute.Detail>(basePath = "unideas://item")),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        ItemDetailScreen(
            itemId = route.itemId,
            initialType = route.initialType,
            promptCompleteOnEntry = route.promptCompleteOnEntry,
            onNavigateBack = onNavigateBack,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToConfig = onNavigateToConfig,
        )
    }
    composable<ItemsRoute.History> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.History>()
        ItemHistoryScreen(itemId = route.itemId, onNavigateBack = { onNavigateBack?.invoke(false) })
    }
    composable<ItemsRoute.Config> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Config>()
        ItemConfigScreen(itemId = route.itemId, isNewItem = route.isNewItem, onNavigateBack = onNavigateBack)
    }
    composable<ItemsRoute.List> {
        ItemsListScreen(
            onNavigateBack = { onNavigateBack?.invoke(false) },
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAddItem = { onNavigateToAddItem(ItemType.TASK) },
        )
    }
}
