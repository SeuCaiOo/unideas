package com.seucaio.unideas.feature.tags.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seucaio.unideas.feature.tags.TagsScreen

fun NavGraphBuilder.tagsNavGraph(onNavigateBack: (() -> Unit)?) {
    composable<TagsRoute.List> {
        TagsScreen(onNavigateBack = onNavigateBack)
    }
}
