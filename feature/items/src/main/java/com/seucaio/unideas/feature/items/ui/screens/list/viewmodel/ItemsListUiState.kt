package com.seucaio.unideas.feature.items.ui.screens.list.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface ItemsListUiState {

    data object Loading : ItemsListUiState

    data class Success(val items: List<Item>) : ItemsListUiState

    data class Error(@StringRes val messageRes: Int) : ItemsListUiState
}
