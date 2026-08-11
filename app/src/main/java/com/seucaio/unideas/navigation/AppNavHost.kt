package com.seucaio.unideas.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.seucaio.unideas.BuildConfig
import com.seucaio.unideas.feature.home.navigation.HomeRoute
import com.seucaio.unideas.feature.home.navigation.homeNavGraph
import com.seucaio.unideas.feature.items.navigation.ItemsRoute
import com.seucaio.unideas.feature.items.navigation.itemsNavGraph
import com.seucaio.unideas.feature.sections.navigation.SectionsRoute
import com.seucaio.unideas.feature.sections.navigation.sectionsNavGraph
import com.seucaio.unideas.feature.settings.navigation.SettingsRoute
import com.seucaio.unideas.feature.settings.navigation.SettingsScreenConfig
import com.seucaio.unideas.feature.settings.navigation.settingsNavGraph
import com.seucaio.unideas.feature.tags.navigation.TagsRoute
import com.seucaio.unideas.feature.tags.navigation.tagsNavGraph

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val topLevelRoute = currentDestination.topLevelRouteOrNull()
    AppScaffold(
        modifier = modifier,
        topLevelRoute = topLevelRoute,
        onNavigateToHome = { navController.navigateToTopLevel(HomeRoute.Panel) },
        onNavigateToAllItems = { navController.navigateToTopLevel(HomeRoute.Browse) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute.Panel,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            homeNavGraph(
                onNavigateBack = navController::popBackStack,
                onNavigateToDetail = { itemId ->
                    navController.navigate(ItemsRoute.Detail(itemId))
                },
                onNavigateToAddItem = { type ->
                    navController.navigate(ItemsRoute.Detail(itemId = null, initialType = type))
                },
                onNavigateToAllPriorities = { navController.navigate(HomeRoute.AllPriorities) },
                onNavigateToSettings = { navController.navigate(SettingsRoute.Settings) },
            )
            settingsNavGraph(
                config = SettingsScreenConfig(
                    versionName = BuildConfig.VERSION_NAME,
                    showDebugSection = BuildConfig.DEBUG,
                ),
                onNavigateBack = navController::popBackStack,
                onNavigateToSections = { navController.navigate(SectionsRoute.List) },
                onNavigateToTags = { navController.navigate(TagsRoute.List) },
                onNavigateToItems = { navController.navigate(ItemsRoute.List) },
            )
            sectionsNavGraph(onNavigateBack = navController::popBackStack)
            tagsNavGraph(onNavigateBack = navController::popBackStack)
            itemsNavGraph(
                onNavigateBack = navController::popBackStack,
                onNavigateToDetail = { itemId ->
                    navController.navigate(ItemsRoute.Detail(itemId))
                },
                onNavigateToAddItem = { type ->
                    navController.navigate(ItemsRoute.Detail(itemId = null, initialType = type))
                },
            )
        }
    }
}
