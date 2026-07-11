package com.seucaio.unideas.core.common.crud

import androidx.annotation.StringRes

/** UI state for a generic named-entity management screen (e.g. Sections, Tags). */
sealed interface EntityCrudUiState<out T> {

    data object Loading : EntityCrudUiState<Nothing>

    data class Success<T>(val items: List<T>) : EntityCrudUiState<T>

    data class Error(@StringRes val messageRes: Int) : EntityCrudUiState<Nothing>
}
