package com.seucaio.unideas.feature.items.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.seucaio.unideas.core.common.dev.ScreenVersion
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.features.detail.screen.ItemDetailScreen
import com.seucaio.unideas.feature.items.features.form.screen.ItemFormScreen
import com.seucaio.unideas.feature.items.features.form.screen.ItemFormSheet
import com.seucaio.unideas.feature.items.features.form.screen.ItemScreen
import com.seucaio.unideas.feature.items.features.list.screen.ItemsListScreen

fun NavGraphBuilder.itemsNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
) {
    composable<ItemsRoute.Form> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Form>()
        when (route.version) {
            ScreenVersion.V1 ->
                ItemFormScreen(itemId = route.itemId, initialType = route.type, onNavigateBack = onNavigateBack)
            ScreenVersion.V2, ScreenVersion.V3, ScreenVersion.V4, ScreenVersion.V5 ->
                ItemScreen(itemId = route.itemId, initialType = route.type, onNavigateBack = onNavigateBack)
        }
    }
    dialog<ItemsRoute.FormSheet>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.FormSheet>()
        ItemFormSheet(initialType = route.type, onNavigateBack = onNavigateBack)
    }
    composable<ItemsRoute.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        ItemDetailScreen(
            itemId = route.itemId,
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit,
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
