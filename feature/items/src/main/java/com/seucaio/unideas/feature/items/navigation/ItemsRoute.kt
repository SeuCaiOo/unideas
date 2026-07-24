package com.seucaio.unideas.feature.items.navigation

import com.seucaio.unideas.domain.model.ItemType
import kotlinx.serialization.Serializable

sealed interface ItemsRoute {

    /**
     * Bottom-sheet destination for creating a new item — registered with `dialog<>` in the nav
     * graph (instead of `composable<>`) so the previous destination stays composed and visible
     * underneath the [ModalBottomSheet], rather than being swapped out as a regular destination
     * transition would do.
     */
    @Serializable
    data class AddItem(val type: ItemType = ItemType.TASK) : ItemsRoute

    @Serializable
    data class Detail(val itemId: Long) : ItemsRoute

    @Serializable
    data object List : ItemsRoute
}
