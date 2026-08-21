package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Archive
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
import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.chips.TextBadge
import com.seucaio.unideas.ds.components.lists.NavCard
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.TitleDescriptionFields
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.components.fields.recurrence.label
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceUiState
import java.time.format.DateTimeFormatter

private val cardTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                NavCard(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.item_config_title),
                    subtitle = configCardSubtitle(state),
                    onClick = onNavigateToConfig,
                    modifier = Modifier.padding(top = 8.dp),
                )

                if (onNavigateToHistory != null) {
                    NavCard(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.item_detail_history),
                        subtitle = stringResource(R.string.item_detail_history_card_subtitle),
                        onClick = onNavigateToHistory,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                ItemFormFooter(
                    state = state,
                    occurrenceState = occurrenceState,
                    onCompleteClicked = onCompleteClicked,
                    onIgnoreClicked = onIgnoreClicked,
                    onExtendDeadlineClicked = onExtendDeadlineClicked,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormBadges(
    type: ItemType,
    isArchived: Boolean,
    onArchivedChipClicked: (() -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        val typeLabelRes =
            if (type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note
        TextBadge(
            text = stringResource(typeLabelRes),
            background = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (isArchived && onArchivedChipClicked != null) {
            Spacer(Modifier.weight(1f))
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
    }
}

@Composable
private fun configCardSubtitle(state: ItemFormFieldsState): String {
    val parts = buildList {
        state.recurrence.label(state.dueDate)?.let(::add)
        state.dueTime?.let { add(it.format(cardTimeFormatter)) }
        state.availableSections.firstOrNull { it.id == state.sectionId }?.let { add(it.name) }
        if (state.selectedTagIds.isNotEmpty()) {
            add(
                pluralStringResource(
                    R.plurals.item_config_card_tags,
                    state.selectedTagIds.size,
                    state.selectedTagIds.size
                )
            )
        }
    }
    return parts.ifEmpty { null }?.joinToString(" · ")
        ?: stringResource(R.string.item_config_card_subtitle_empty)
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
