package com.seucaio.unideas.feature.items.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.ui.screens.additem.ItemFormSheet
import com.seucaio.unideas.feature.items.ui.screens.form.ItemScreen
import com.seucaio.unideas.feature.items.ui.screens.list.ItemsListScreen

fun NavGraphBuilder.itemsNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
) {
    dialog<ItemsRoute.FormSheet>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.FormSheet>()
        ItemFormSheet(
            initialType = route.type,
            onNavigateBack = onNavigateBack
        )
    }
    composable<ItemsRoute.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        ItemScreen(
            itemId = route.itemId,
            onNavigateBack = onNavigateBack
        )
    }
    composable<ItemsRoute.List> {
        ItemsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToForm = { onNavigateToForm(ItemType.TASK) },
        )
    }
}
