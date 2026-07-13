package com.seucaio.unideas.feature.tags.navigation

import kotlinx.serialization.Serializable

sealed interface TagsRoute {

    @Serializable
    data object List : TagsRoute
}
