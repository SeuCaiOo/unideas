package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.components.inputs.SelectionBottomSheet
import com.seucaio.unideas.ds.components.inputs.SelectionSheetContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun RecurrenceField(
    recurrence: Recurrence,
    onRecurrenceChanged: (Recurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    DateFieldButton(
        valueLabel = recurrence.label(),
        defaultValue = stringResource(R.string.item_form_recurrence_none),
        onClick = { showBottomSheet = true },
        onClear = { onRecurrenceChanged(Recurrence.None) },
        clearContentDescription = stringResource(R.string.item_form_recurrence_clear),
        modifier = modifier,
    )

    if (showBottomSheet) {
        RecurrenceBottomSheet(
            recurrence = recurrence,
            onRecurrenceSelected = {
                onRecurrenceChanged(it)
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false },
        )
    }
}

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

private fun recurrenceOptions(recurrence: Recurrence): List<Recurrence> {
    val everyNDays = recurrence as? Recurrence.EveryNDays
        ?: Recurrence.EveryNDays(Recurrence.EveryNDays.EVERY_OTHER_DAY_DAYS)
    return listOf(Recurrence.Daily, Recurrence.Weekly, Recurrence.Monthly, everyNDays)
}

@Composable
internal fun Recurrence.label(): String? = when (this) {
    Recurrence.None -> null
    Recurrence.Daily -> stringResource(R.string.item_form_recurrence_daily)
    Recurrence.Weekly -> stringResource(R.string.item_form_recurrence_weekly)
    Recurrence.Monthly -> stringResource(R.string.item_form_recurrence_monthly)
    is Recurrence.EveryNDays -> stringResource(R.string.item_form_recurrence_every_n_days, days)
}

/**
 * One provider covering every scenario this file's previews need: the field empty (default,
 * nothing picked), the field filled (clearable via the "X"), and the bottom sheet's content
 * (`ModalBottomSheet` itself never renders in a static `@Preview` — see `.claude/rules/mvi.md`).
 */
private sealed interface RecurrencePreviewScenario {
    data object EmptyField : RecurrencePreviewScenario
    data object FilledField : RecurrencePreviewScenario
    data object SheetContent : RecurrencePreviewScenario
}

private class RecurrenceFieldPreviewProvider : PreviewParameterProvider<RecurrencePreviewScenario> {
    override val values = sequenceOf(
        RecurrencePreviewScenario.EmptyField,
        RecurrencePreviewScenario.FilledField,
        RecurrencePreviewScenario.SheetContent,
    )
}

@PreviewLightDark
@Composable
private fun RecurrenceFieldPreview(
    @PreviewParameter(RecurrenceFieldPreviewProvider::class) scenario: RecurrencePreviewScenario,
) {
    UdsTheme {
        Surface {
            when (scenario) {
                RecurrencePreviewScenario.EmptyField -> RecurrenceField(
                    recurrence = Recurrence.None,
                    onRecurrenceChanged = {},
                    modifier = Modifier.padding(16.dp),
                )
                RecurrencePreviewScenario.FilledField -> RecurrenceField(
                    recurrence = Recurrence.Weekly,
                    onRecurrenceChanged = {},
                    modifier = Modifier.padding(16.dp),
                )
                RecurrencePreviewScenario.SheetContent -> SelectionSheetContent(
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
}
