package com.seucaio.unideas.core.common.crud

/** Which entity dialog (if any) is showing on top of a generic management screen. */
sealed interface EntityDialogState<out T> {

    data object None : EntityDialogState<Nothing>

    data object Add : EntityDialogState<Nothing>

    data class Rename<T>(val item: T) : EntityDialogState<T>

    data class Delete<T>(val item: T) : EntityDialogState<T>
}
