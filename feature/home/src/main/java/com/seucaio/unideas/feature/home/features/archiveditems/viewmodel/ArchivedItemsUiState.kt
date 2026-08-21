package com.seucaio.unideas.feature.home.features.archiveditems.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

sealed interface ArchivedItemsUiState {

    data object Loading : ArchivedItemsUiState

    data class Success(val items: List<Item>) : ArchivedItemsUiState

    data class Error(@param:StringRes val messageRes: Int) : ArchivedItemsUiState
}
