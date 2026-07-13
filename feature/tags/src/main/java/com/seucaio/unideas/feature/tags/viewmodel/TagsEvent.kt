package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.domain.model.Tag

/** User interactions on the manage-tags screen. */
sealed interface TagsEvent {

    data object OnAddClicked : TagsEvent

    data class OnAddConfirmClicked(val name: String) : TagsEvent

    data class OnRenameClicked(val tag: Tag) : TagsEvent

    data class OnRenameConfirmClicked(val newName: String) : TagsEvent

    data class OnDeleteClicked(val tag: Tag) : TagsEvent

    data object OnDeleteConfirmClicked : TagsEvent

    data object OnDialogDismissed : TagsEvent

    data object OnRetryClicked : TagsEvent
}
