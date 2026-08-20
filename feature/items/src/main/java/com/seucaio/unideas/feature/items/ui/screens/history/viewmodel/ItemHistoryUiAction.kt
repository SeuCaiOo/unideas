package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import androidx.annotation.StringRes

sealed interface ItemHistoryUiAction {
    data class ShowSnackbar(@param:StringRes val resId: Int) : ItemHistoryUiAction
    data class ShowError(val message: String) : ItemHistoryUiAction
}
