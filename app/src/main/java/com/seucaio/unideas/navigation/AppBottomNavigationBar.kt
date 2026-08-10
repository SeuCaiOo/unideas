package com.seucaio.unideas.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.seucaio.unideas.R
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.navigation.HomeRoute

@Composable
internal fun AppBottomNavigationBar(
    selectedRoute: HomeRoute?,
    onNavigateToHome: () -> Unit,
    onNavigateToAllItems: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedRoute is HomeRoute.Panel,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.bottom_nav_home)) },
        )
        NavigationBarItem(
            selected = selectedRoute is HomeRoute.Browse,
            onClick = onNavigateToAllItems,
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.bottom_nav_all_items)) },
        )
    }
}

internal fun NavDestination?.topLevelRouteOrNull(): HomeRoute? = when {
    isOnRoute<HomeRoute.Panel>() -> HomeRoute.Panel
    isOnRoute<HomeRoute.Browse>() -> HomeRoute.Browse
    else -> null
}

internal fun NavController.navigateToTopLevel(route: HomeRoute) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private inline fun <reified T : HomeRoute> NavDestination?.isOnRoute(): Boolean =
    this?.hierarchy?.any { it.hasRoute<T>() } == true

private class AppBottomNavigationBarPreviewProvider : PreviewParameterProvider<HomeRoute?> {
    override val values = sequenceOf(HomeRoute.Panel, HomeRoute.Browse)
}

@PreviewLightDark
@Composable
private fun AppBottomNavigationBarPreview(
    @PreviewParameter(AppBottomNavigationBarPreviewProvider::class) selectedRoute: HomeRoute?,
) {
    UdsTheme {
        Surface {
            AppBottomNavigationBar(
                selectedRoute = selectedRoute,
                onNavigateToHome = {},
                onNavigateToAllItems = {},
            )
        }
    }
}
