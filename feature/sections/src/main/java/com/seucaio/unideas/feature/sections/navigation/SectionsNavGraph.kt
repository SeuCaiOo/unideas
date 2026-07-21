package com.seucaio.unideas.feature.sections.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.feature.sections.SectionsScreen

fun NavGraphBuilder.sectionsNavGraph(onNavigateBack: (() -> Unit)?) {
    composable<SectionsRoute.List> {
        SectionsScreen(onNavigateBack = onNavigateBack)
    }
}
