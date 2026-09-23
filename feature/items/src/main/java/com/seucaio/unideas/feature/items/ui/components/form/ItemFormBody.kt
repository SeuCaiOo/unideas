package com.seucaio.unideas.feature.items.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.ItemStatus
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.chips.TextBadge
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.TitleDescriptionFields
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsEvents
import com.seucaio.unideas.feature.items.ui.components.fields.model.ItemFormFieldsState
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.ItemDetailPreviewProvider
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailUiState

private val SNACKBAR_RESERVED_HEIGHT = 72.dp

@Composable
fun ItemFormBody(
    state: ItemFormFieldsState,
    events: ItemFormFieldsEvents,
    modifier: Modifier = Modifier,
    isArchived: Boolean = false,
    onUnarchiveClicked: (() -> Unit)? = null,
    isConfidential: Boolean = false,
    isSnackbarVisible: Boolean = false,
    sections: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val minHeight = maxHeight

        Column(
            modifier = Modifier
                .heightIn(min = minHeight)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            ItemFormBadges(
                type = state.type,
                isArchived = isArchived,
                onArchivedChipClicked = onUnarchiveClicked,
                isConfidential = isConfidential,
            )

            TitleDescriptionFields(
                title = state.title,
                description = state.description,
                onTitleChanged = events.onTitleChanged,
                onDescriptionChanged = events.onDescriptionChanged,
                onDescriptionCheckboxToggled = events.onDescriptionCheckboxToggled,
                isEditing = state.isEditing,
                titleError = state.titleError,
            )

            Spacer(modifier = Modifier.weight(1f))

            sections()

            Spacer(modifier = Modifier.height(if (isSnackbarVisible) SNACKBAR_RESERVED_HEIGHT else 0.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormBadges(
    type: ItemType,
    isArchived: Boolean,
    onArchivedChipClicked: (() -> Unit)?,
    isConfidential: Boolean,
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
        if (isConfidential) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                TextBadge(
                    text = stringResource(R.string.item_detail_confidential_badge),
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    content = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
                    onDescriptionCheckboxToggled = {},
                ),
                isArchived = previewState.status == ItemStatus.ARCHIVED,
                onUnarchiveClicked = {},
                isConfidential = previewState.isConfidential,
                sections = {},
            )
        }
    }
}
