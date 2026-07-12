package com.seucaio.unideas.feature.home.features.allpriorities.viewmodel

import androidx.annotation.StringRes
import com.seucaio.unideas.domain.model.Item

/** UI state for the all-priorities screen — the full, uncapped list behind Home's "See all". */
sealed interface AllPrioritiesUiState {

    data object Loading : AllPrioritiesUiState

    data class Success(val items: List<Item>) : AllPrioritiesUiState

    data class Error(@StringRes val messageRes: Int) : AllPrioritiesUiState
}
