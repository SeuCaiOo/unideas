package com.seucaio.unideas.feature.items.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.seucaio.unideas.feature.items.ItemDetailScreen
import com.seucaio.unideas.feature.items.ItemFormScreen

fun NavGraphBuilder.itemsNavGraph(onNavigateBack: (() -> Unit)?, onNavigateToEdit: (Long) -> Unit) {
    composable<ItemsRoute.Form> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Form>()
        ItemFormScreen(itemId = route.itemId, onNavigateBack = onNavigateBack)
    }
    composable<ItemsRoute.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        ItemDetailScreen(itemId = route.itemId, onNavigateBack = onNavigateBack, onNavigateToEdit = onNavigateToEdit)
    }
}
