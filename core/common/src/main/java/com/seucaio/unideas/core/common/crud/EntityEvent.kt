package com.seucaio.unideas.core.common.crud

/** User interactions on a generic named-entity management screen. */
sealed interface EntityEvent<out T> {

    data object OnAddClicked : EntityEvent<Nothing>

    data class OnAddConfirmClicked(val name: String) : EntityEvent<Nothing>

    data class OnRenameClicked<T>(val item: T) : EntityEvent<T>

    data class OnRenameConfirmClicked(val newName: String) : EntityEvent<Nothing>

    data class OnDeleteClicked<T>(val item: T) : EntityEvent<T>

    data object OnDeleteConfirmClicked : EntityEvent<Nothing>

    data object OnDialogDismissed : EntityEvent<Nothing>

    data object OnRetryClicked : EntityEvent<Nothing>
}
