package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel

import androidx.annotation.StringRes

sealed interface ItemConfigUiAction {

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : ItemConfigUiAction

    data class ShowError(val message: String) : ItemConfigUiAction
}
