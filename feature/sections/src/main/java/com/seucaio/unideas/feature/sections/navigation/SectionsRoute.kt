package com.seucaio.unideas.feature.sections.navigation

import kotlinx.serialization.Serializable

sealed interface SectionsRoute {

    @Serializable
    data object List : SectionsRoute
}
