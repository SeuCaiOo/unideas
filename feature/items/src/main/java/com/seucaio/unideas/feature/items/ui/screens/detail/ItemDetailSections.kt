package com.seucaio.unideas.feature.items.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.ds.components.lists.ConfigSummaryNavCard
import com.seucaio.unideas.ds.components.lists.HistorySummaryNavCard
import com.seucaio.unideas.ds.components.lists.NavCardConfigItem
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.CompletionField
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueDate
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueTime
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableRecurrence
import com.seucaio.unideas.feature.items.ui.components.fields.recurrence.label
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.ItemLinksSection
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel.ItemLinksEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel.ItemLinksUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val cardTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ItemDetailSections(
    uiState: ItemDetailUiState,
    occurrenceState: ItemOccurrenceUiState,
    linksState: ItemLinksUiState,
    onOccurrenceEvent: (ItemOccurrenceEvent) -> Unit,
    onLinksEvent: (ItemLinksEvent) -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    onNavigateToConfig: (Long) -> Unit,
    onAddLinkType: (ItemType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemId = uiState.itemId ?: return

    Column(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ConfigSummaryNavCard(
            title = stringResource(R.string.item_config_title),
            rows = configSummaryRows(uiState),
            onClick = { onNavigateToConfig(itemId) },
        )

        if (!uiState.isConfidential) {
            ItemLinksSection(uiState = linksState, onEvent = onLinksEvent, onAddType = onAddLinkType)
        }

        if (uiState.recurrence != Recurrence.None) {
            HistorySection(occurrenceState, onClick = { onNavigateToHistory(itemId) })
        }

        if (uiState.typeIsTask) {
            CompletionSection(uiState, occurrenceState, onOccurrenceEvent)
        }
    }
}

@Composable
private fun CompletionSection(
    uiState: ItemDetailUiState,
    occurrenceState: ItemOccurrenceUiState,
    onOccurrenceEvent: (ItemOccurrenceEvent) -> Unit,
) {
    val canMuteReminders = uiState.reminderWarning != ReminderWarning.None &&
        !occurrenceState.isCompleted && !occurrenceState.isLate
    CompletionField(
        isCompleted = occurrenceState.isCompleted,
        isLate = occurrenceState.isLate,
        completedLate = occurrenceState.completedLate,
        completedAt = occurrenceState.completedAt,
        overdueDays = occurrenceState.dueDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() },
        onCompleteClicked = { onOccurrenceEvent(ItemOccurrenceEvent.OnCompleteClicked) },
        onExtendDeadlineClicked = { onOccurrenceEvent(ItemOccurrenceEvent.OnExtendDeadlineClicked) },
        onIgnoreClicked = if (occurrenceState.canIgnore) {
            { onOccurrenceEvent(ItemOccurrenceEvent.OnIgnoreClicked) }
        } else {
            null
        },
        remindersMuted = occurrenceState.remindersMuted,
        onMuteRemindersToggled = if (canMuteReminders) {
            { onOccurrenceEvent(ItemOccurrenceEvent.OnMuteRemindersToggled) }
        } else {
            null
        },
    )
}

@Composable
private fun HistorySection(
    occurrenceState: ItemOccurrenceUiState,
    onClick: () -> Unit,
) {
    HistorySummaryNavCard(
        title = stringResource(R.string.item_detail_history),
        lines = listOf(
            stringResource(R.string.item_detail_history_on_time_percent, occurrenceState.historyOnTimePercent),
            pluralStringResource(
                R.plurals.item_history_occurrence_count,
                occurrenceState.historyCount,
                occurrenceState.historyCount,
            ),
        ),
        onClick = onClick,
    )
}

@Composable
private fun configSummaryRows(state: ItemFormFieldsState): List<List<NavCardConfigItem>> {
    val recurrence = state.persistableRecurrence
    val dueDate = state.persistableDueDate
    val recurrenceItem = if (recurrence != Recurrence.None) {
        recurrence.label(dueDate)?.let { NavCardConfigItem(Icons.Outlined.Repeat, it) }
    } else {
        dueDate?.let { NavCardConfigItem(Icons.Outlined.Event, it.toFormattedDateString()) }
    }
    val timeItem = state.persistableDueTime?.let {
        NavCardConfigItem(Icons.Outlined.Schedule, it.format(cardTimeFormatter))
    }
    val sectionItem = state.availableSections.firstOrNull { it.id == state.sectionId }?.let {
        NavCardConfigItem(Icons.Outlined.Folder, it.name)
    }
    val tagsItem = if (state.selectedTagIds.isNotEmpty()) {
        NavCardConfigItem(
            Icons.Outlined.Sell,
            pluralStringResource(R.plurals.item_config_card_tags, state.selectedTagIds.size, state.selectedTagIds.size),
        )
    } else {
        null
    }

    val rows = listOf(
        listOfNotNull(recurrenceItem, timeItem),
        listOfNotNull(sectionItem, tagsItem),
    ).filter { it.isNotEmpty() }

    return rows.ifEmpty {
        listOf(
            listOf(NavCardConfigItem(Icons.Outlined.Event, stringResource(R.string.item_config_card_subtitle_empty)))
        )
    }
}

@PreviewLightDark
@Composable
private fun ItemDetailSectionsPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) previewState: ItemDetailUiState,
) {
    UdsTheme {
        Surface {
            ItemDetailSections(
                uiState = previewState,
                occurrenceState = ItemOccurrenceUiState(),
                linksState = ItemLinksUiState.Success(),
                onOccurrenceEvent = {},
                onLinksEvent = {},
                onNavigateToHistory = {},
                onNavigateToConfig = {},
                onAddLinkType = {},
            )
        }
    }
}
