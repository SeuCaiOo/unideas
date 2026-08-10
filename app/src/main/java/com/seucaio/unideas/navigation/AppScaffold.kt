package com.seucaio.unideas.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.navigation.HomeRoute

@Composable
internal fun AppScaffold(
    topLevelRoute: HomeRoute?,
    onNavigateToHome: () -> Unit,
    onNavigateToAllItems: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (topLevelRoute != null) {
                AppBottomNavigationBar(
                    selectedRoute = topLevelRoute,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToAllItems = onNavigateToAllItems,
                )
            }
        },
    ) { padding ->
        content(padding)
    }
}

private class AppScaffoldPreviewProvider : PreviewParameterProvider<HomeRoute?> {
    override val values = sequenceOf(HomeRoute.Panel, HomeRoute.Browse, null)
}

@PreviewLightDark
@Composable
private fun AppScaffoldPreview(
    @PreviewParameter(AppScaffoldPreviewProvider::class) topLevelRoute: HomeRoute?,
) {
    UdsTheme {
        AppScaffold(
            topLevelRoute = topLevelRoute,
            onNavigateToHome = {},
            onNavigateToAllItems = {},
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Screen content")
                }
            }
        }
    }
}
