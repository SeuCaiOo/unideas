package com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface HiddenItemsUiState {

    data object Loading : HiddenItemsUiState

    data class Success(val items: List<Item>) : HiddenItemsUiState

    data class Error(@param:StringRes val messageRes: Int) : HiddenItemsUiState
}
