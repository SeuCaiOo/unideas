package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel

sealed interface ItemLinkPickerUiAction {
    data object NavigateBack : ItemLinkPickerUiAction
    data class ShowError(val message: String) : ItemLinkPickerUiAction
}
