package com.seucaio.unideas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UdsTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute.Panel,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    homeNavGraph(
                        onNavigateBack = navController::popBackStack,
                        onNavigateToDetail = navController::navigateToItemDetail,
                        onNavigateToForm = navController::navigateToItemForm,
                        onNavigateToAllPriorities = { navController.navigate(HomeRoute.AllPriorities) },
                        onNavigateToSettings = { navController.navigate(SettingsRoute.Settings) },
                        onNavigateToBrowse = { navController.navigate(HomeRoute.Browse) },
                    )
                    settingsNavGraph(
                        config = SettingsScreenConfig(
                            versionName = BuildConfig.VERSION_NAME,
                            showDebugSection = BuildConfig.DEBUG,
                        ),
                        onNavigateBack = navController::popBackStack,
                        onNavigateToSections = { navController.navigate(SectionsRoute.List) },
                        onNavigateToTags = { navController.navigate(TagsRoute.List) },
                        // Debug-only entry point — Home is the real one now.
                        onNavigateToItems = { navController.navigate(ItemsRoute.List) },
                    )
                    sectionsNavGraph(onNavigateBack = navController::popBackStack)
                    tagsNavGraph(onNavigateBack = navController::popBackStack)
                    itemsNavGraph(
                        onNavigateBack = navController::popBackStack,
                        onNavigateToDetail = navController::navigateToItemDetail,
                        onNavigateToForm = navController::navigateToItemForm,
                    )
                }
            }
        }
    }
}

/** Creating a new item always opens the dialog-hosted bottom sheet (#86/#97). */
private fun NavController.navigateToItemForm(type: ItemType) {
    navigate(ItemsRoute.FormSheet(type = type))
}

/** Tapping an item lands directly on its full-screen destination, already editable. */
private fun NavController.navigateToItemDetail(itemId: Long) {
    navigate(ItemsRoute.Detail(itemId))
}
