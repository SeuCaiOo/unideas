package com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel

import androidx.annotation.StringRes

sealed interface ItemDetailDialogState {

    data object None : ItemDetailDialogState

    data object DeleteConfirm : ItemDetailDialogState

    data class DiscardConfirm(
        @param:StringRes val titleRes: Int,
        @param:StringRes val messageRes: Int
    ) : ItemDetailDialogState

    data object UnarchiveConfirm : ItemDetailDialogState
}
