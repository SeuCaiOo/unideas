package com.seucaio.unideas.feature.tags.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Tag

/** UI state for the manage-tags screen. */
sealed interface TagsUiState {

    data object Loading : TagsUiState

    data class Success(
        val tags: List<Tag>,
        val dialog: TagsDialogState = TagsDialogState.None,
    ) : TagsUiState

    data class Error(@StringRes val messageRes: Int) : TagsUiState
}
