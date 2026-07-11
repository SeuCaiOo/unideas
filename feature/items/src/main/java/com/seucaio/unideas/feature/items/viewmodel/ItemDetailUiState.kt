package com.seucaio.unideas.feature.items.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

/** UI state for the item detail screen. */
sealed interface ItemDetailUiState {

    data object Loading : ItemDetailUiState

    data class Success(val item: Item) : ItemDetailUiState

    data class Error(@StringRes val messageRes: Int) : ItemDetailUiState
}
