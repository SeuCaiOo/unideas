package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.picker.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface ItemLinkPickerUiState {

    data object Loading : ItemLinkPickerUiState

    data class Success(
        val items: List<Item> = emptyList(),
        val selectedIds: Set<Long> = emptySet()
    ) : ItemLinkPickerUiState

    data class Error(@param:StringRes val messageRes: Int) : ItemLinkPickerUiState
}
