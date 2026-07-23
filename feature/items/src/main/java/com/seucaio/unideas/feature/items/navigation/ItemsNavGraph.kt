package com.seucaio.unideas.feature.items.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.seucaio.unideas.core.common.dev.ScreenVersion
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.ui.screens.detail.ItemFormSheet
import com.seucaio.unideas.feature.items.ui.screens.form.ItemFormScreen
import com.seucaio.unideas.feature.items.ui.screens.form.ItemScreen
import com.seucaio.unideas.feature.items.ui.screens.list.ItemsListScreen

fun NavGraphBuilder.itemsNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
) {
    composable<ItemsRoute.Form> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Form>()
        when (route.version) {
            ScreenVersion.V1 ->
                _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.form.ItemFormScreen(
                    itemId = route.itemId,
                    initialType = route.type,
                    onNavigateBack = onNavigateBack
                )
            ScreenVersion.V2, ScreenVersion.V3, ScreenVersion.V4, ScreenVersion.V5 ->
                _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.form.ItemScreen(
                    itemId = route.itemId,
                    initialType = route.type,
                    onNavigateBack = onNavigateBack
                )
        }
    }
    dialog<ItemsRoute.FormSheet>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.FormSheet>()
        _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.detail.ItemFormSheet(
            initialType = route.type,
            onNavigateBack = onNavigateBack
        )
    }
    composable<ItemsRoute.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.form.ItemScreen(
            itemId = route.itemId,
            onNavigateBack = onNavigateBack
        )
    }
    composable<ItemsRoute.List> {
        _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.list.ItemsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToForm = { onNavigateToForm(ItemType.TASK) },
        )
    }
}
