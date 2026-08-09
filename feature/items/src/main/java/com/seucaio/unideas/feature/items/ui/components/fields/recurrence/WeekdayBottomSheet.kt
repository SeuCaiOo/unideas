package com.seucaio.unideas.feature.items.ui.components.fields.recurrence

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.components.inputs.SelectionBottomSheet
import com.seucaio.unideas.ds.components.inputs.SelectionSheetContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.DayOfWeek
import java.time.format.TextStyle

@Composable
fun WeekdayBottomSheet(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionBottomSheet(
        title = stringResource(R.string.item_form_recurrence_weekday_title),
        options = DayOfWeek.entries,
        selectedOption = selectedDay,
        optionLabel = { it.label() },
        onOptionSelected = onDaySelected,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun DayOfWeek.label(): String {
    val locale = LocalConfiguration.current.locales[0]
    return getDisplayName(TextStyle.FULL, locale)
}

@PreviewLightDark
@Composable
private fun WeekdayBottomSheetPreview() {
    UdsTheme {
        Surface {
            SelectionSheetContent(
                title = stringResource(R.string.item_form_recurrence_weekday_title),
                description = null,
                options = DayOfWeek.entries,
                selectedOption = DayOfWeek.TUESDAY,
                optionLabel = { it.label() },
                onOptionSelected = {},
            )
        }
    }
}
