package com.seucaio.unideas.feature.items.features.detail.viewmodel

/** User interactions on the item detail screen. */
sealed interface ItemDetailEvent {

    data object OnDeleteClicked : ItemDetailEvent

    data object OnDeleteConfirmClicked : ItemDetailEvent

    data object OnDialogDismissed : ItemDetailEvent

    data object OnEditClicked : ItemDetailEvent

    data object OnCompleteClicked : ItemDetailEvent

    data object OnShareClicked : ItemDetailEvent

    data object OnRetryClicked : ItemDetailEvent
}
