package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

sealed interface ItemDetailDialogState {

    data object None : ItemDetailDialogState

    data object DeleteConfirm : ItemDetailDialogState
}
