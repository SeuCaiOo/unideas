package com.seucaio.unideas.feature.home.features.home.screen.components.chrome

import androidx.compose.runtime.Composable
import com.seucaio.unideas.ds.components.legacy.ConfirmationDialog
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeDialogState
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeEvent
import com.seucaio.unideas.feature.home.features.priority.screen.PriorityBottomSheet

@Composable
internal fun HomeDialogs(
    showPriorityBottomSheet: Boolean,
    onPriorityBottomSheetDismiss: () -> Unit,
    dialogState: HomeDialogState,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllPriorities: () -> Unit,
    onEvent: (HomeEvent) -> Unit,
) {
    if (showPriorityBottomSheet) {
        PriorityBottomSheet(
            onDismiss = onPriorityBottomSheetDismiss,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAllPriorities = onNavigateToAllPriorities,
        )
    }

    if (dialogState is HomeDialogState.DeleteSelectedConfirm) {
        ConfirmationDialog(
            titleRes = R.string.home_delete_selected_title,
            messageRes = R.string.home_delete_selected_message,
            onDismiss = { onEvent(HomeEvent.OnDeleteDialogDismissed) },
            onConfirm = { onEvent(HomeEvent.OnDeleteSelectedConfirmClicked) },
        )
    }
}
