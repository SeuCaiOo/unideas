package com.seucaio.unideas.feature.items.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.ui.screens.additem.AddItemSheet
import com.seucaio.unideas.feature.items.ui.screens.detail.ItemDetailScreen
import com.seucaio.unideas.feature.items.ui.screens.list.ItemsListScreen

fun NavGraphBuilder.itemsNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddItem: (ItemType) -> Unit,
) {
    dialog<ItemsRoute.AddItem>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.AddItem>()
        AddItemSheet(
            initialType = route.type,
            onNavigateBack = onNavigateBack
        )
    }
    composable<ItemsRoute.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        ItemDetailScreen(
            itemId = route.itemId,
            onNavigateBack = onNavigateBack
        )
    }
    composable<ItemsRoute.List> {
        ItemsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAddItem = { onNavigateToAddItem(ItemType.TASK) },
        )
    }
}
