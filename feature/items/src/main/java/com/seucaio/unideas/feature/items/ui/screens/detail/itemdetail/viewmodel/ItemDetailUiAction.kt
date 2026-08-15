package com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface ItemDetailUiAction {

    data object NavigateBack : ItemDetailUiAction

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : ItemDetailUiAction

    data class ShowError(val message: String) : ItemDetailUiAction

    data class ShareText(val item: Item) : ItemDetailUiAction

    data class ItemPersisted(val item: Item) : ItemDetailUiAction
}
