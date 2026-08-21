package com.seucaio.unideas.navigation

import android.content.Intent
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.seucaio.unideas.BuildConfig
import com.seucaio.unideas.feature.home.navigation.HomeRoute
import com.seucaio.unideas.feature.home.navigation.homeNavGraph
import com.seucaio.unideas.feature.items.navigation.ItemsRoute
import com.seucaio.unideas.feature.items.navigation.itemsNavGraph
import com.seucaio.unideas.feature.onboarding.navigation.OnboardingRoute
import com.seucaio.unideas.feature.onboarding.navigation.onboardingNavGraph
import com.seucaio.unideas.feature.sections.navigation.SectionsRoute
import com.seucaio.unideas.feature.sections.navigation.sectionsNavGraph
import com.seucaio.unideas.feature.settings.navigation.SettingsRoute
import com.seucaio.unideas.feature.settings.navigation.SettingsScreenConfig
import com.seucaio.unideas.feature.settings.navigation.settingsNavGraph
import com.seucaio.unideas.feature.tags.navigation.TagsRoute
import com.seucaio.unideas.feature.tags.navigation.tagsNavGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    initialIntent: Intent,
    needsOnboarding: Boolean,
    modifier: Modifier = Modifier
) {
    AppScaffold(modifier = modifier) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (needsOnboarding) OnboardingRoute.Login else HomeRoute.Home,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            appDestinations(navController)
        }

        // Scaffold subcomposes this content slot separately from the outer composition, so the
        // graph NavHost just attached above is only guaranteed to exist for code that runs in this
        // same slot — handling the deep link from the caller's composition instead races with it
        // and can hit NavController with no graph set yet.
        LaunchedEffect(Unit) { navController.handleDeepLink(initialIntent) }
    }
}

private fun NavGraphBuilder.appDestinations(navController: NavHostController) {
    onboardingNavGraph(
        onOnboardingComplete = {
            navController.navigate(HomeRoute.Home) {
                popUpTo(OnboardingRoute.Login) { inclusive = true }
            }
        },
    )
    homeNavGraph(
        onNavigateBack = navController::popBackStack,
        onNavigateToDetail = { itemId ->
            navController.navigate(ItemsRoute.Detail(itemId))
        },
        onNavigateToAddItem = { type ->
            navController.navigate(ItemsRoute.Detail(itemId = null, initialType = type))
        },
        onNavigateToAllPriorities = { navController.navigate(HomeRoute.AllPriorities) },
        onNavigateToArchivedItems = { navController.navigate(HomeRoute.ArchivedItems) },
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
        onNavigateToHistory = { itemId ->
            navController.navigate(ItemsRoute.History(itemId))
        },
        onNavigateToConfig = { itemId, isNewItem ->
            navController.navigate(ItemsRoute.Config(itemId, isNewItem))
        },
    )
}
