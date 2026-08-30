package com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface ItemOccurrenceUiAction {

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : ItemOccurrenceUiAction

    data class ShowError(val message: String) : ItemOccurrenceUiAction

    data class ItemPersisted(val item: Item) : ItemOccurrenceUiAction

    data object NavigateBack : ItemOccurrenceUiAction
}
