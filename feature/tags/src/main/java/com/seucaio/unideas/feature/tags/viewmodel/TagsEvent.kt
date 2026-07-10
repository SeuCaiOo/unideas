package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.domain.model.Tag

/** User interactions on the manage-tags screen. */
sealed interface TagsEvent {

    data class OnAddClicked(val name: String) : TagsEvent

    data class OnRenameClicked(val tag: Tag, val newName: String) : TagsEvent

    data class OnDeleteClicked(val id: Long) : TagsEvent
}
