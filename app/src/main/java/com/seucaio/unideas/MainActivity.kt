package com.seucaio.unideas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.feature.items.navigation.ItemsRoute
import com.seucaio.unideas.feature.items.navigation.itemsNavGraph
import com.seucaio.unideas.feature.sections.navigation.SectionsRoute
import com.seucaio.unideas.feature.sections.navigation.sectionsNavGraph
import com.seucaio.unideas.feature.settings.navigation.SettingsRoute
import com.seucaio.unideas.feature.settings.navigation.settingsNavGraph
import com.seucaio.unideas.feature.tags.navigation.TagsRoute
import com.seucaio.unideas.feature.tags.navigation.tagsNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnideasTheme {
                val navController = rememberNavController()
                // SettingsRoute.Settings is a placeholder startDestination until Home (#27)
                // exists — it's the natural temporary root since it's the screen that fans out
                // to Sections/Tags. No back stack to pop from it yet, so no back button (null).
                NavHost(
                    navController = navController,
                    startDestination = SettingsRoute.Settings,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    settingsNavGraph(
                        versionName = BuildConfig.VERSION_NAME,
                        onNavigateBack = null,
                        onNavigateToSections = { navController.navigate(SectionsRoute.List) },
                        onNavigateToTags = { navController.navigate(TagsRoute.List) },
                    )
                    sectionsNavGraph(onNavigateBack = navController::popBackStack)
                    tagsNavGraph(onNavigateBack = navController::popBackStack)
                    // No entry point yet — the FAB that opens this (Home, #27) doesn't exist.
                    itemsNavGraph(
                        onNavigateBack = navController::popBackStack,
                        onNavigateToEdit = { itemId -> navController.navigate(ItemsRoute.Form(itemId)) },
                    )
                }
            }
        }
    }
}
