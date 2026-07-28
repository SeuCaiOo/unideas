package com.seucaio.unideas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
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

    // Held outside setContent so onNewIntent (app already running, e.g. tapped from a reminder
    // notification while some other screen is open) can hand the new deep link to the same
    // NavController instead of only working on a cold start.
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UdsTheme {
                val navController = rememberNavController()
                this.navController = navController
                LaunchedEffect(Unit) { navController.handleDeepLink(intent) }
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute.Panel,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    homeNavGraph(
                        onNavigateBack = navController::popBackStack,
                        onNavigateToDetail = { itemId ->
                            navController.navigate(ItemsRoute.Detail(itemId))
                        },
                        onNavigateToAddItem = { type ->
                            navController.navigate(ItemsRoute.AddItem(type))
                        },
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
                            navController.navigate(ItemsRoute.AddItem(type))
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.handleDeepLink(intent)
    }
}
