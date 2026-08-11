package com.seucaio.unideas.feature.home.features.panel.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(val hasAnyItem: Boolean) : HomeUiState

    data class Error(@param:StringRes val messageRes: Int) : HomeUiState
}

data class HomeItemsState(
    val priorityItems: List<Item> = emptyList(),
    val showSeeAllButton: Boolean = false,
)
