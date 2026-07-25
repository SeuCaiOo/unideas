package com.seucaio.unideas.feature.home.navigation

import kotlinx.serialization.Serializable

sealed interface HomeRoute {

    @Serializable
    data object Panel : HomeRoute

    @Serializable
    data object AllPriorities : HomeRoute

    @Serializable
    data object Browse : HomeRoute
}
