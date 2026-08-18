package com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel

import com.seucaio.unideas.domain.model.Item

sealed interface ItemDetailEvent {

    sealed interface FieldEvent : ItemDetailEvent

    data class OnTitleChanged(val title: String) : FieldEvent

    data class OnDescriptionChanged(val description: String) : FieldEvent

    data object OnShareClicked : ItemDetailEvent

    data object OnDeleteClicked : ItemDetailEvent

    data object OnDeleteConfirmClicked : ItemDetailEvent

    data object OnDialogDismissed : ItemDetailEvent

    data object OnRetryClicked : ItemDetailEvent

    data object OnBackRequested : ItemDetailEvent

    data object OnDiscardConfirmed : ItemDetailEvent

    data class OnItemUpdatedExternally(val item: Item) : ItemDetailEvent

    data object OnScreenResumed : ItemDetailEvent
}
