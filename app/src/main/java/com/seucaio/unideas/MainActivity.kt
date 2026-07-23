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
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.core.common.dev.ScreenVersion
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
                        onNavigateToEdit = navController::navigateToItemEdit,
                        onNavigateToDetail = navController::navigateToItemDetail,
                        onNavigateToForm = navController::navigateToItemForm,
                    )
                }
            }
        }
    }
}

/**
 * Centralizes the create-form routing decision so every caller (Home, Items list, Settings
 * debug entry) automatically picks up the right destination type per POC version, without
 * each Screen needing to know about it. [ScreenVersion.V4] reuses [ScreenVersion.V2]'s
 * dialog-hosted [FormSheet] for creation — it has no bottom sheet variant of its own (#86/#97).
 */
private fun NavController.navigateToItemForm(type: ItemType) {
    val version = DevScreenVersionToggle.selectedVersion.value
    if (version == ScreenVersion.V2 || version == ScreenVersion.V4) {
        navigate(ItemsRoute.FormSheet(type = type))
    } else {
        navigate(ItemsRoute.Form(type = type))
    }
}

/** Edit entry point from within [ItemDetailScreen]'s edit button — V1/V2/V3 only, V4 has no detail screen to edit from. */
private fun NavController.navigateToItemEdit(itemId: Long) {
    if (DevScreenVersionToggle.selectedVersion.value == ScreenVersion.V2) {
        navigate(ItemsRoute.FormSheet(itemId = itemId))
    } else {
        navigate(ItemsRoute.Form(itemId = itemId))
    }
}

/**
 * [ScreenVersion.V4] skips [ItemDetailScreen] entirely: tapping an item lands directly on
 * [ItemFormScreenV4]'s regular full-screen destination, already editable — no separate
 * read-only state, no extra "editar" tap.
 */
private fun NavController.navigateToItemDetail(itemId: Long) {
    if (DevScreenVersionToggle.selectedVersion.value == ScreenVersion.V4) {
        navigate(ItemsRoute.Form(itemId = itemId))
    } else {
        navigate(ItemsRoute.Detail(itemId))
    }
}
