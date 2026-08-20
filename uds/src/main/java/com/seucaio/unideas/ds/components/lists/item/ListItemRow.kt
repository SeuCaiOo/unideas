package com.seucaio.unideas.ds.components.lists.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.lists.model.ListItemUi
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.leftAccentBorder

private const val DESCRIPTION_COLLAPSED_LINES = 1
private const val DESCRIPTION_EXPANDED_LINES = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemRow(
    ui: ListItemUi,
    onClick: () -> Unit,
    onToggleCheck: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onLongClick: (() -> Unit)? = null,
    onToggleSelection: (() -> Unit)? = null,
    onTogglePin: (() -> Unit)? = null,
) {
    val accentColor = when {
        ui.isPinned -> MaterialTheme.colorScheme.primary
        ui.badgeLabel != null -> ui.badgeColor
        else -> Color.Transparent
    }
    var descriptionExpanded by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(containerColor)
            .leftAccentBorder(4.dp, accentColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.showCheckbox) {
                ListItemCheckbox(ui.checked, ui.checkContentDescription, onToggleCheck)
            }
            ListItemTitleAndMeta(ui, modifier = Modifier.weight(1f))
            if (ui.isSelected != null) {
                SelectionIndicator(ui.isSelected, onToggle = { onToggleSelection?.invoke() })
            } else {
                NormalTrailingContent(ui, onTogglePin)
            }
        }
        if (!ui.description.isNullOrBlank()) {
            ListItemDescription(
                description = ui.description,
                expanded = descriptionExpanded,
                onToggleExpanded = { descriptionExpanded = !descriptionExpanded },
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun ListItemTitleAndMeta(ui: ListItemUi, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            ui.title,
            style = MaterialTheme.typography.headlineMedium,
            color = if (ui.checked) {
                LocalUdsExtendedColors.current.textTertiary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (ui.checked) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!ui.meta.isNullOrEmpty()) {
                Text(
                    ui.meta,
                    style = MaterialTheme.typography.titleSmall,
                    color = LocalUdsExtendedColors.current.textTertiary
                )
            }
            if (ui.showRepeatIcon) {
                Icon(
                    Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = LocalUdsExtendedColors.current.textTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ListItemDescription(
    description: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var canExpand by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) DESCRIPTION_EXPANDED_LINES else DESCRIPTION_COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> if (!expanded) canExpand = result.hasVisualOverflow },
            modifier = Modifier.weight(1f),
        )
        if (canExpand) {
            IconButton(onClick = onToggleExpanded, modifier = Modifier.size(20.dp)) {
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private enum class ListItemRowPreviewScenario {
    Default, Pinned, WithShortDescription, WithLongDescription, SelectedInSelectionMode, UnselectedInSelectionMode
}

private class ListItemRowPreviewProvider : PreviewParameterProvider<ListItemRowPreviewScenario> {
    override val values = ListItemRowPreviewScenario.entries.asSequence()
}

@Composable
private fun OverdueBillRow(
    isPinned: Boolean = false,
    description: String? = null,
    isSelected: Boolean? = null,
) {
    ListItemRow(
        ui = ListItemUi(
            id = 1L, title = "Pay electricity bill", meta = "Home", showCheckbox = true,
            checked = false, showRepeatIcon = true, badgeLabel = "6 days overdue",
            badgeColor = MaterialTheme.colorScheme.error, checkContentDescription = "Confirm",
            isPinned = isPinned, description = description, isSelected = isSelected,
        ),
        onClick = {},
        onToggleCheck = {},
        onToggleSelection = {},
        onTogglePin = {},
    )
}

@PreviewLightDark
@Composable
private fun ListItemRowPreview(
    @PreviewParameter(ListItemRowPreviewProvider::class) scenario: ListItemRowPreviewScenario,
) {
    UdsTheme {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            when (scenario) {
                ListItemRowPreviewScenario.Default -> OverdueBillRow()
                ListItemRowPreviewScenario.Pinned -> OverdueBillRow(isPinned = true)
                ListItemRowPreviewScenario.WithShortDescription ->
                    OverdueBillRow(description = "Pay before the due date to avoid a late fee.")
                ListItemRowPreviewScenario.WithLongDescription -> OverdueBillRow(
                    description = "Check the meter reading before paying — last month's bill looked " +
                        "unusually high compared to the last six months, so it might be worth calling " +
                        "the provider to confirm there wasn't a billing error before just paying it off " +
                        "without asking any questions.",
                )
                ListItemRowPreviewScenario.SelectedInSelectionMode -> OverdueBillRow(isSelected = true)
                ListItemRowPreviewScenario.UnselectedInSelectionMode -> OverdueBillRow(isSelected = false)
            }
        }
    }
}
