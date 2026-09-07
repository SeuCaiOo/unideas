package com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel

sealed interface HiddenItemsEvent {

    data class OnItemClicked(val itemId: Long) : HiddenItemsEvent

    data object OnRetryClicked : HiddenItemsEvent
}
