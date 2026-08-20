package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.sectionstags

import androidx.annotation.StringRes

sealed interface SectionsTagsUiAction {
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : SectionsTagsUiAction
    data class ShowError(val message: String) : SectionsTagsUiAction
}
