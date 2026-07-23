package com.seucaio.unideas.feature.items.features.form.viewmodel

/** Dialog state for the create/edit item form — currently only the delete confirmation. */
sealed interface ItemFormDialogState {

    data object None : ItemFormDialogState

    data object DeleteConfirm : ItemFormDialogState
}
