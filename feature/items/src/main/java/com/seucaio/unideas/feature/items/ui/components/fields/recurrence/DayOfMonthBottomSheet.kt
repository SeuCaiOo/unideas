package com.seucaio.unideas.feature.items.ui.components.fields.recurrence

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.components.inputs.GridSelectionBottomSheet
import com.seucaio.unideas.ds.components.inputs.GridSelectionSheetContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

private val DAYS_OF_MONTH = (1..31).toList()

@Composable
fun DayOfMonthBottomSheet(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    GridSelectionBottomSheet(
        title = stringResource(R.string.item_form_recurrence_day_of_month_title),
        options = DAYS_OF_MONTH,
        selectedOption = selectedDay,
        optionLabel = { it.toString() },
        onOptionSelected = onDaySelected,
        onDismiss = onDismiss,
    )
}

@PreviewLightDark
@Composable
private fun DayOfMonthBottomSheetPreview() {
    UdsTheme {
        Surface {
            GridSelectionSheetContent(
                title = stringResource(R.string.item_form_recurrence_day_of_month_title),
                description = null,
                options = DAYS_OF_MONTH,
                selectedOption = 15,
                optionLabel = { it.toString() },
                onOptionSelected = {},
            )
        }
    }
}
