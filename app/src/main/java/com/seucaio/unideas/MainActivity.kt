package com.seucaio.unideas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.navigation.AppNavHost
import com.seucaio.unideas.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    // Held outside setContent so onNewIntent (app already running, e.g. tapped from a reminder
    // notification while some other screen is open) can hand the new deep link to the same
    // NavController instead of only working on a cold start.
    private var navController: NavHostController? = null

    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val needsOnboarding by viewModel.needsOnboarding.collectAsStateWithLifecycle()
            needsOnboarding?.let { resolvedNeedsOnboarding ->
                AppRoot(
                    needsOnboarding = resolvedNeedsOnboarding,
                    initialIntent = intent,
                    onNavControllerReady = { navController = it },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.handleDeepLink(intent)
    }
}

@Composable
private fun AppRoot(
    needsOnboarding: Boolean,
    initialIntent: Intent,
    onNavControllerReady: (NavHostController) -> Unit,
) {
    UdsTheme {
        val navController = rememberNavController()
        onNavControllerReady(navController)
        AppNavHost(navController, initialIntent = initialIntent, needsOnboarding = needsOnboarding)
    }
}
