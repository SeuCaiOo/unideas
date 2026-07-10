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
import com.seucaio.unideas.feature.sections.navigation.SectionsRoute
import com.seucaio.unideas.feature.sections.navigation.sectionsNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnideasTheme {
                val navController = rememberNavController()
                // SectionsRoute.List is a placeholder startDestination until Home (#27) exists —
                // no back stack to pop yet, so no back button (null) until Home takes over as root.
                NavHost(
                    navController = navController,
                    startDestination = SectionsRoute.List,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    sectionsNavGraph(onNavigateBack = null)
                }
            }
        }
    }
}
