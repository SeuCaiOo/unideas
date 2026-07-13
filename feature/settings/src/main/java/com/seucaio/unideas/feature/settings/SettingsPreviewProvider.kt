package com.seucaio.unideas.feature.settings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState

class SettingsPreviewProvider : PreviewParameterProvider<SettingsUiState> {

    override val values: Sequence<SettingsUiState> = sequenceOf(
        SettingsUiState.Success,
    )
}
