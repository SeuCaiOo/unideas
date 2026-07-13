package com.seucaio.unideas.feature.items.features.detail.viewmodel

/** One-shot UI actions for the item detail screen. */
sealed interface ItemDetailUiAction {

    /** The item was deleted — the Screen should pop the back stack. */
    data object NavigateBack : ItemDetailUiAction

    data class NavigateToEdit(val itemId: Long) : ItemDetailUiAction

    /** The Screen should launch an Android share intent with [text]. */
    data class ShareText(val text: String) : ItemDetailUiAction

    /** Unexpected repository failure — raw exception message, not localized. */
    data class ShowError(val message: String) : ItemDetailUiAction
}
