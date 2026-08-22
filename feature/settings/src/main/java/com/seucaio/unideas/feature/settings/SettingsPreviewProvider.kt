package com.seucaio.unideas.feature.settings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.feature.settings.viewmodel.SettingsAccountUiState
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState

data class SettingsScreenPreviewScenario(
    val uiState: SettingsUiState,
    val accountUiState: SettingsAccountUiState = SettingsAccountUiState(),
)

class SettingsPreviewProvider : PreviewParameterProvider<SettingsScreenPreviewScenario> {

    override val values: Sequence<SettingsScreenPreviewScenario> = sequenceOf(
        SettingsScreenPreviewScenario(
            uiState = SettingsUiState.Success,
            accountUiState = SettingsAccountUiState(
                isConnected = true,
                accountName = "Caio Pimentel",
                accountEmail = "caio@example.com",
            ),
        ),
        SettingsScreenPreviewScenario(uiState = SettingsUiState.Success),
    )
}
