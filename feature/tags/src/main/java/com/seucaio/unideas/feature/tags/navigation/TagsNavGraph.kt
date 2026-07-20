package com.seucaio.unideas.feature.tags.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.feature.tags.TagsScreen
import com.seucaio.unideas.feature.tags.TagsScreenV2

fun NavGraphBuilder.tagsNavGraph(onNavigateBack: (() -> Unit)?) {
    composable<TagsRoute.List> {
        val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
        if (useV2) {
            TagsScreenV2(onNavigateBack = onNavigateBack)
        } else {
            TagsScreen(onNavigateBack = onNavigateBack)
        }
    }
}
