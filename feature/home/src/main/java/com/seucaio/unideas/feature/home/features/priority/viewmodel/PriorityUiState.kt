package com.seucaio.unideas.feature.home.features.priority.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface PriorityUiState {

    data object Loading : PriorityUiState

    data class Success(val hasAnyItem: Boolean) : PriorityUiState

    data class Error(@param:StringRes val messageRes: Int) : PriorityUiState
}

data class PriorityItemsState(
    val priorityItems: List<Item> = emptyList(),
    val showSeeAllButton: Boolean = false,
)
