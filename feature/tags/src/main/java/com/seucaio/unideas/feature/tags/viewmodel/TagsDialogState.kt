package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.domain.model.Tag

/** Which entity dialog (if any) is showing on top of the tags list. */
sealed interface TagsDialogState {

    data object None : TagsDialogState

    data object Add : TagsDialogState

    data class Rename(val tag: Tag) : TagsDialogState

    data class Delete(val tag: Tag) : TagsDialogState
}
