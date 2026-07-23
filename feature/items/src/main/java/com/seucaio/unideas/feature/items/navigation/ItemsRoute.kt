package com.seucaio.unideas.feature.items.navigation

import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.core.common.dev.ScreenVersion
import com.seucaio.unideas.domain.model.ItemType
import kotlinx.serialization.Serializable

sealed interface ItemsRoute {

    /**
     * `itemId == null` creates a new item; otherwise edits the existing one. [type] only
     * applies to creation — it's ignored (the loaded item's own type wins) when editing.
     * [version] picks which POC implementation (#86 Pacote 2) renders this route — defaults to
     * whatever's selected in Settings' debug screen-version picker at the time of navigation.
     */
    @Serializable
    data class Form(
        val itemId: Long? = null,
        val type: ItemType = ItemType.TASK,
        val version: ScreenVersion = DevScreenVersionToggle.selectedVersion.value,
    ) : ItemsRoute

    @Serializable
    data class Detail(val itemId: Long) : ItemsRoute

    /** Dev-only listing until Home (#27) exists — see #62. */
    @Serializable
    data object List : ItemsRoute
}
