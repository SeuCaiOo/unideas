package com.seucaio.unideas.feature.items.navigation

import com.seucaio.unideas.domain.model.ItemType
import kotlinx.serialization.Serializable

sealed interface ItemsRoute {

    @Serializable
    data class Detail(val itemId: Long? = null, val initialType: ItemType = ItemType.TASK) : ItemsRoute

    @Serializable
    data class History(val itemId: Long) : ItemsRoute

    @Serializable
    data class Config(val itemId: Long, val isNewItem: Boolean = false) : ItemsRoute

    @Serializable
    data object List : ItemsRoute
}
