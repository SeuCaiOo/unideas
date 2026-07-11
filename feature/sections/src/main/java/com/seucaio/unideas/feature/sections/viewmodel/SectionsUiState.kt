package com.seucaio.unideas.feature.sections.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Section

/** UI state for the manage-sections screen. */
sealed interface SectionsUiState {

    data object Loading : SectionsUiState

    data class Success(val sections: List<Section>) : SectionsUiState

    data class Error(@StringRes val messageRes: Int) : SectionsUiState
}
