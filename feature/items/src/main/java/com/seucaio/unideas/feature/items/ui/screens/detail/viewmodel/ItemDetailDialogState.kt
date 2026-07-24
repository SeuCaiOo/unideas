package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

/** Dialog state for the create/edit item form — currently only the delete confirmation. */
sealed interface ItemDetailDialogState {

    data object None : ItemDetailDialogState

    data object DeleteConfirm : ItemDetailDialogState
}
