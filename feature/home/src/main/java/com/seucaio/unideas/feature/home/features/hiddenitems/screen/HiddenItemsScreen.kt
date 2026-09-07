package com.seucaio.unideas.feature.home.features.hiddenitems.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.biometric.BiometricAuthResult
import com.seucaio.unideas.core.common.biometric.BiometricAuthenticator
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel.HiddenItemsUiAction
import com.seucaio.unideas.feature.home.features.hiddenitems.viewmodel.HiddenItemsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HiddenItemsScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HiddenItemsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val title = stringResource(R.string.hidden_items_biometric_title)
    var mode by remember {
        mutableStateOf<HiddenItemsMode>(HiddenItemsMode.Gating(HiddenItemsGateState.Authenticating))
    }

    val authenticate: () -> Unit = {
        val activity = context as? FragmentActivity
        if (activity == null) {
            mode = HiddenItemsMode.Gating(HiddenItemsGateState.Failed(R.string.hidden_items_biometric_unavailable))
        } else {
            mode = HiddenItemsMode.Gating(HiddenItemsGateState.Authenticating)
            BiometricAuthenticator.authenticate(activity = activity, title = title) { result ->
                mode = when (result) {
                    BiometricAuthResult.Success -> HiddenItemsMode.Unlocked
                    BiometricAuthResult.Unavailable ->
                        HiddenItemsMode.Gating(HiddenItemsGateState.Failed(R.string.hidden_items_biometric_unavailable))

                    BiometricAuthResult.Failed ->
                        HiddenItemsMode.Gating(HiddenItemsGateState.Failed(R.string.hidden_items_biometric_failed))
                }
            }
        }
    }

    LaunchedEffect(Unit) { authenticate() }

    when (val itemsMode = mode) {
        is HiddenItemsMode.Gating ->
            HiddenItemsGateContent(
                state = itemsMode.state,
                onNavigateBack = onNavigateBack,
                onRetryAuth = authenticate,
            )

        HiddenItemsMode.Unlocked ->
            HiddenItemsUnlockedRoute(
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onNavigateToDetail = onNavigateToDetail,
            )
    }
}

@Composable
private fun HiddenItemsUnlockedRoute(
    viewModel: HiddenItemsViewModel,
    onNavigateBack: (() -> Unit)?,
    onNavigateToDetail: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is HiddenItemsUiAction.NavigateToDetail -> updatedOnNavigateToDetail(action.itemId)
                is HiddenItemsUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    HiddenItemsListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onEvent = viewModel::onEvent,
    )
}
