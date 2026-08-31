package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.chips.TextBadge
import com.seucaio.unideas.ds.components.lists.ConfigSummaryNavCard
import com.seucaio.unideas.ds.components.lists.HistorySummaryNavCard
import com.seucaio.unideas.ds.components.lists.NavCardConfigItem
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.TitleDescriptionFields
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueDate
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableDueTime
import com.seucaio.unideas.feature.items.ui.components.fields.model.persistableRecurrence
import com.seucaio.unideas.feature.items.ui.components.fields.recurrence.label
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiState
import java.time.format.DateTimeFormatter

private val cardTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Approximate height of a one-line Snackbar; reserved as bottom padding while one is showing, so it
 * doesn't cover [ItemFormFooter]'s completion status. */
private val SNACKBAR_RESERVED_HEIGHT = 72.dp

@Composable
fun ItemFormBody(
    state: ItemFormFieldsState,
    events: ItemFormFieldsEvents,
    occurrenceState: ItemOccurrenceUiState,
    onCompleteClicked: () -> Unit,
    onIgnoreClicked: () -> Unit,
    onExtendDeadlineClicked: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToHistory: (() -> Unit)?,
    modifier: Modifier = Modifier,
    isArchived: Boolean = false,
    onUnarchiveClicked: (() -> Unit)? = null,
    isSnackbarVisible: Boolean = false,
    onMuteRemindersToggled: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            ItemFormBadges(
                type = state.type,
                isArchived = isArchived,
                onArchivedChipClicked = onUnarchiveClicked
            )

            TitleDescriptionFields(
                title = state.title,
                description = state.description,
                onTitleChanged = events.onTitleChanged,
                onDescriptionChanged = events.onDescriptionChanged,
                isEditing = state.isEditing,
                titleError = state.titleError,
            )
        }

        if (state.isEditing) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (isSnackbarVisible) SNACKBAR_RESERVED_HEIGHT else 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ItemFormFooter(
                    state = state,
                    occurrenceState = occurrenceState,
                    onCompleteClicked = onCompleteClicked,
                    onIgnoreClicked = onIgnoreClicked,
                    onExtendDeadlineClicked = onExtendDeadlineClicked,
                    onMuteRemindersToggled = onMuteRemindersToggled,
                )

                HistoryAndConfigCards(
                    state = state,
                    occurrenceState = occurrenceState,
                    onNavigateToConfig = onNavigateToConfig,
                    onNavigateToHistory = onNavigateToHistory,
                )
            }
        }
    }
}

@Composable
private fun HistoryAndConfigCards(
    state: ItemFormFieldsState,
    occurrenceState: ItemOccurrenceUiState,
    onNavigateToConfig: () -> Unit,
    onNavigateToHistory: (() -> Unit)?,
) {
    if (onNavigateToHistory != null) {
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
            onClick = onNavigateToHistory,
        )
    }

    ConfigSummaryNavCard(
        title = stringResource(R.string.item_config_title),
        rows = configSummaryRows(state),
        onClick = onNavigateToConfig,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormBadges(
    type: ItemType,
    isArchived: Boolean,
    onArchivedChipClicked: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        if (isArchived && onArchivedChipClicked != null) {
            FilterChip(
                selected = true,
                onClick = onArchivedChipClicked,
                label = { Text(stringResource(R.string.item_detail_archived_badge)) },
                leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
        val typeLabelRes =
            if (type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note
        TextBadge(
            text = stringResource(typeLabelRes),
            background = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
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
private fun ItemFormBodyPreview(
    @PreviewParameter(ItemDetailPreviewProvider::class) previewState: ItemDetailUiState,
) {
    UdsTheme {
        Surface {
            ItemFormBody(
                state = previewState,
                events = ItemFormFieldsEvents(
                    onTitleChanged = {},
                    onDescriptionChanged = {},
                ),
                occurrenceState = ItemOccurrenceUiState(),
                isArchived = previewState.status == ItemStatus.ARCHIVED,
                onUnarchiveClicked = {},
                onCompleteClicked = {},
                onIgnoreClicked = {},
                onExtendDeadlineClicked = {},
                onNavigateToConfig = {},
                onNavigateToHistory = {},
            )
        }
    }
}
