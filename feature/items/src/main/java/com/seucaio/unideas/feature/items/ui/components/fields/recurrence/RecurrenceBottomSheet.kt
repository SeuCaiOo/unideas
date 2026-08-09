package com.seucaio.unideas.feature.items.ui.components.fields.recurrence

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.SelectionBottomSheet
import com.seucaio.unideas.ds.components.inputs.SelectionSheetContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun RecurrenceBottomSheet(
    recurrence: Recurrence,
    onRecurrenceSelected: (Recurrence) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionBottomSheet(
        title = stringResource(R.string.item_form_recurrence_label),
        options = recurrenceOptions(recurrence),
        selectedOption = recurrence,
        optionLabel = { it.label().orEmpty() },
        onOptionSelected = onRecurrenceSelected,
        onDismiss = onDismiss,
    )
}

internal fun recurrenceOptions(recurrence: Recurrence): List<Recurrence> {
    val everyNDays = recurrence as? Recurrence.EveryNDays
        ?: Recurrence.EveryNDays(Recurrence.EveryNDays.EVERY_OTHER_DAY_DAYS)
    return listOf(Recurrence.Daily, Recurrence.Weekly, Recurrence.Monthly, everyNDays)
}

@PreviewLightDark
@Composable
private fun RecurrenceBottomSheetPreview() {
    UdsTheme {
        Surface {
            SelectionSheetContent(
                title = stringResource(R.string.item_form_recurrence_label),
                description = null,
                options = recurrenceOptions(Recurrence.Weekly),
                selectedOption = Recurrence.Weekly,
                optionLabel = { it.label().orEmpty() },
                onOptionSelected = {},
            )
        }
    }
}
