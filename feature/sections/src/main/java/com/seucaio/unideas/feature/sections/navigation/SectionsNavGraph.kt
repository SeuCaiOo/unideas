package com.seucaio.unideas.feature.sections.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.feature.sections.SectionsScreen
import com.seucaio.unideas.feature.sections.SectionsScreenV2

fun NavGraphBuilder.sectionsNavGraph(onNavigateBack: (() -> Unit)?) {
    composable<SectionsRoute.List> {
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            SectionsScreenV2(onNavigateBack = onNavigateBack)
        } else {
            SectionsScreen(onNavigateBack = onNavigateBack)
        }
    }
}
