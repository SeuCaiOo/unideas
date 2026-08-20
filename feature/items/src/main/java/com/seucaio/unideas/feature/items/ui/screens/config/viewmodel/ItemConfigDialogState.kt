package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel

import com.seucaio.unideas.domain.model.ItemType

sealed interface ItemConfigDialogState {

    data object None : ItemConfigDialogState

    data class TypeSwitchConfirm(val newType: ItemType) : ItemConfigDialogState
}
