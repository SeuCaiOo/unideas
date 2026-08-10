package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.inputs.CollapsibleSection
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailUiState
import java.time.LocalDate

/**
 * All of [ItemFormBody]'s secondary options — [ItemFormCommonOptions] (section/tags, both types)
 * plus [ItemFormTaskOptions] (due date/time/recurrence/reminder, task-only) — behind one collapsed
 * toggle, so the type/task-vs-note visibility rules live in a single place instead of being
 * re-checked at each call site.
 */
@Composable
fun ItemFormOptionsSection(
    state: ItemFormFieldsState,
    events: ItemFormFieldsEvents,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    CollapsibleSection(
        title = stringResource(R.string.item_form_more_options),
        expanded = expanded,
        onToggle = { expanded = !expanded },
        modifier = modifier,
    ) {
        ItemFormCommonOptions(state, events)

        if (state.typeIsTask) {
            ItemFormTaskOptions(state, events, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

private val noopEvents = ItemFormFieldsEvents(
    onTypeChanged = {},
    onTitleChanged = {},
    onDescriptionChanged = {},
    onSectionChanged = {},
    onTagToggled = {},
    onReminderToggled = {},
    onDueDateChanged = {},
    onDueTimeChanged = {},
    onRecurrenceChanged = {},
    onReminderWarningChanged = {},
)

/** Task with the reminder switch on: every field (section, tags, date, time, recurrence, reminder). */
@PreviewLightDark
@Composable
private fun ItemFormOptionsSectionTaskFullPreview() {
    UdsTheme {
        Surface {
            ItemFormOptionsSection(
                state = ItemDetailUiState(
                    type = ItemType.TASK,
                    hasReminder = true,
                    dueDate = LocalDate.of(2026, 8, 1),
                ),
                events = noopEvents,
                initiallyExpanded = true,
            )
        }
    }
}

/** Task with the reminder switch off: only section/tags — no date/time/recurrence/reminder fields. */
@PreviewLightDark
@Composable
private fun ItemFormOptionsSectionTaskMinimalPreview() {
    UdsTheme {
        Surface {
            ItemFormOptionsSection(
                state = ItemDetailUiState(type = ItemType.TASK),
                events = noopEvents,
                initiallyExpanded = true,
            )
        }
    }
}

/** Note: no due fields at all (task-only), and collapsed by default. */
@PreviewLightDark
@Composable
private fun ItemFormOptionsSectionNoteCollapsedPreview() {
    UdsTheme {
        Surface {
            ItemFormOptionsSection(
                state = ItemDetailUiState(type = ItemType.NOTE),
                events = noopEvents,
            )
        }
    }
}
