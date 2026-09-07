package com.seucaio.unideas.feature.home.features.hiddenitems.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.ds.components.legacy.UnideasErrorContent
import com.seucaio.unideas.ds.components.legacy.UnideasLoadingContent
import com.seucaio.unideas.ds.components.legacy.UnideasTopBar
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R

@Composable
internal fun HiddenItemsGateContent(
    state: HiddenItemsGateState,
    onNavigateBack: (() -> Unit)?,
    onRetryAuth: () -> Unit,
) {
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)

    Scaffold(
        topBar = {
            UnideasTopBar(
                title = stringResource(R.string.hidden_items_title),
                onNavigateBack = updatedOnNavigateBack,
            )
        },
    ) { padding ->
        when (state) {
            is HiddenItemsGateState.Authenticating ->
                UnideasLoadingContent(modifier = Modifier.padding(padding))

            is HiddenItemsGateState.Failed ->
                UnideasErrorContent(
                    messageRes = state.messageRes,
                    onRetry = onRetryAuth,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

private class HiddenItemsGateStatePreviewProvider : PreviewParameterProvider<HiddenItemsGateState> {
    override val values = sequenceOf(
        HiddenItemsGateState.Authenticating,
        HiddenItemsGateState.Failed(R.string.hidden_items_biometric_unavailable),
        HiddenItemsGateState.Failed(R.string.hidden_items_biometric_failed),
    )
}

@PreviewLightDark
@Composable
private fun HiddenItemsGateContentPreview(
    @PreviewParameter(HiddenItemsGateStatePreviewProvider::class) state: HiddenItemsGateState,
) {
    UdsTheme {
        HiddenItemsGateContent(
            state = state,
            onNavigateBack = {},
            onRetryAuth = {},
        )
    }
}
