package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface ItemLinksUiState {

    data object Loading : ItemLinksUiState

    data class Success(val linkedItems: List<Item> = emptyList()) : ItemLinksUiState

    data class Error(@param:StringRes val messageRes: Int) : ItemLinksUiState
}
