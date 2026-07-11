package com.seucaio.unideas.feature.items.viewmodel

/** Which dialog (if any) is showing on top of the item detail screen. */
sealed interface ItemDetailDialogState {

    data object None : ItemDetailDialogState

    data object DeleteConfirm : ItemDetailDialogState
}
