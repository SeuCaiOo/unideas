package com.seucaio.unideas.feature.items.navigation

import kotlinx.serialization.Serializable

sealed interface ItemsRoute {

    /** `itemId == null` creates a new item; otherwise edits the existing one. */
    @Serializable
    data class Form(val itemId: Long? = null) : ItemsRoute

    @Serializable
    data class Detail(val itemId: Long) : ItemsRoute

    /** Dev-only listing until Home (#27) exists — see #62. */
    @Serializable
    data object List : ItemsRoute
}
