package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.annotation.StringRes

sealed interface ItemDetailDialogState {

    data object None : ItemDetailDialogState

    data object DeleteConfirm : ItemDetailDialogState

    data object ReopenConfirm : ItemDetailDialogState

    data object History : ItemDetailDialogState

    data class DiscardConfirm(
        @param:StringRes val titleRes: Int,
        @param:StringRes val messageRes: Int
    ) : ItemDetailDialogState
}
