package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel

sealed interface ItemLinkPickerEvent {
    data class OnItemToggled(val itemId: Long) : ItemLinkPickerEvent
    data object OnConfirmClicked : ItemLinkPickerEvent
    data object OnRetryClicked : ItemLinkPickerEvent
}
