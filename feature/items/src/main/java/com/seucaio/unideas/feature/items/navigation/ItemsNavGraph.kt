package com.seucaio.unideas.feature.items.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.items.features.detail.screen.ItemDetailScreen
import com.seucaio.unideas.feature.items.features.detail.screen.ItemDetailScreenV2
import com.seucaio.unideas.feature.items.features.form.screen.ItemFormScreen
import com.seucaio.unideas.feature.items.features.form.screen.ItemFormScreenV2
import com.seucaio.unideas.feature.items.features.list.screen.ItemsListScreen

fun NavGraphBuilder.itemsNavGraph(
    onNavigateBack: (() -> Unit)?,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (ItemType) -> Unit,
) {
    composable<ItemsRoute.Form> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Form>()
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            ItemFormScreenV2(
                itemId = route.itemId,
                initialType = route.type,
                onNavigateBack = onNavigateBack
            )
        } else {
            ItemFormScreen(
                itemId = route.itemId,
                initialType = route.type,
                onNavigateBack = onNavigateBack
            )
        }
    }
    composable<ItemsRoute.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemsRoute.Detail>()
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            ItemDetailScreenV2(
                itemId = route.itemId,
                onNavigateBack = onNavigateBack,
                onNavigateToEdit = onNavigateToEdit,
            )
        } else {
            ItemDetailScreen(
                itemId = route.itemId,
                onNavigateBack = onNavigateBack,
                onNavigateToEdit = onNavigateToEdit,
            )
        }
    }
    composable<ItemsRoute.List> {
        ItemsListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToForm = { onNavigateToForm(ItemType.TASK) },
        )
    }
}
