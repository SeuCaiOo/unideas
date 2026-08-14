package com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel

import androidx.annotation.StringRes

sealed interface ItemOccurrenceUiAction {

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : ItemOccurrenceUiAction

    data class ShowError(val message: String) : ItemOccurrenceUiAction
}
