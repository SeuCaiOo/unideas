package com.seucaio.unideas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.navigation.AppNavHost

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
                AppNavHost(navController, initialIntent = intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.handleDeepLink(intent)
    }
}
