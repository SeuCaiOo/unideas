package com.seucaio.unideas.feature.items.features.form.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.features.form.screen.components.ItemFormBody
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormEvent
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiAction
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormUiState
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * #86 Pacote 2, direção V2: renders as a [ModalBottomSheet] (`skipPartiallyExpanded = true`, so it
 * opens fully expanded like a screen) instead of a regular destination — the other visual variant
 * under comparison, alongside V3/V4's plain `Scaffold` destinations. No top bar — the sheet's own
 * dismiss (drag/back/outside tap) covers "voltar"; the save action lives inside the shared
 * [ItemFormBody] (full-width button at the end of the form) instead of a toolbar action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreenV2(
    itemId: Long?,
    onNavigateBack: (() -> Unit)?,
    initialType: ItemType = ItemType.TASK,
    viewModel: ItemFormViewModel = koinViewModel { parametersOf(itemId, initialType) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val updatedOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is ItemFormUiAction.NavigateBack -> updatedOnNavigateBack?.invoke()
                is ItemFormUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                    resources.getString(action.messageRes)
                )
                is ItemFormUiAction.ShowError -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { updatedOnNavigateBack?.invoke() },
        sheetState = sheetState,
    ) {
        ItemFormV2Content(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun ItemFormV2Content(
    uiState: ItemFormUiState,
    onEvent: (ItemFormEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Column {
        ItemFormBody(state = uiState, onEvent = onEvent)
        SnackbarHost(hostState = snackbarHostState)
    }
}

@PreviewLightDark
@Composable
private fun ItemFormScreenV2Preview(
    @PreviewParameter(ItemFormPreviewProvider::class) previewState: ItemFormPreviewState,
) {
    UdsTheme {
        Surface {
            ItemFormV2Content(
                uiState = previewState.uiState,
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
