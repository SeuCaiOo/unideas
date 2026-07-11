package com.seucaio.unideas.feature.items.navigation

import kotlinx.serialization.Serializable

sealed interface ItemsRoute {

    /** `itemId == null` creates a new item; otherwise edits the existing one. */
    @Serializable
    data class Form(val itemId: Long? = null) : ItemsRoute
}
