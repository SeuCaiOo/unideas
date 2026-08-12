package com.seucaio.unideas.ds.components.lists.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.seucaio.unideas.ds.theme.pinnedContainerColor

/**
 * Grid-cell counterpart to [ListItemRow] — same [ListItemUi], title **above** meta/badge instead
 * of beside it. [ListItemRow]'s side-by-side layout only has room for a title when it can claim
 * most of a full-width row; squeezed into a half-width grid cell the title has nowhere to go
 * (confirmed on-device: title disappears entirely, leaving only the badge). Stacking vertically
 * gives the title the cell's full width on its own line, `maxLines = 2` since cells are narrower
 * than a full-width row.
 */
@Deprecated("Avoid — grid layout, don't extend or use.")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemCard(
    ui: ListItemUi,
    onClick: () -> Unit,
    onToggleCheck: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onLongClick: (() -> Unit)? = null,
    onToggleSelection: (() -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(containerColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            if (ui.showCheckbox) {
                ListItemCheckbox(ui.checked, ui.checkContentDescription, onToggleCheck, size = 20.dp, iconSize = 14.dp)
            }
            Text(
                ui.title,
                style = MaterialTheme.typography.headlineMedium,
                color = if (ui.checked) {
                    LocalUdsExtendedColors.current.textTertiary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (ui.checked) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!ui.meta.isNullOrEmpty()) {
                Text(
                    ui.meta,
                    style = MaterialTheme.typography.titleSmall,
                    color = LocalUdsExtendedColors.current.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // Bounded so a long recurrence label can never squeeze the badge below —
                    // fill = false lets it shrink to its own content when that's shorter.
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            if (ui.showRepeatIcon) {
                Icon(
                    Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = LocalUdsExtendedColors.current.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            ListItemTrailingIndicator(ui, onToggleSelection, size = 22.dp)
        }
    }
}

private enum class ListItemCardPreviewScenario {
    Default, Pinned, LongTitle, SelectedInSelectionMode, UnselectedInSelectionMode
}

private class ListItemCardPreviewProvider : PreviewParameterProvider<ListItemCardPreviewScenario> {
    override val values = ListItemCardPreviewScenario.entries.asSequence()
}

@Composable
private fun overdueBillUi(isSelected: Boolean? = null) = ListItemUi(
    id = 1L, title = "Pay electricity bill", meta = "Home", showCheckbox = true,
    checked = false, showRepeatIcon = true, badgeLabel = "6 days overdue",
    badgeColor = MaterialTheme.colorScheme.error, checkContentDescription = "Confirm",
    isSelected = isSelected,
)

@PreviewLightDark
@Composable
private fun ListItemCardPreview(
    @PreviewParameter(ListItemCardPreviewProvider::class) scenario: ListItemCardPreviewScenario,
) {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp).width(170.dp)) {
            when (scenario) {
                ListItemCardPreviewScenario.Default ->
                    ListItemCard(ui = overdueBillUi(), onClick = {}, onToggleCheck = {})
                ListItemCardPreviewScenario.Pinned -> ListItemCard(
                    ui = overdueBillUi(),
                    onClick = {},
                    onToggleCheck = {},
                    containerColor = pinnedContainerColor(
                        isPinned = true,
                        base = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
                ListItemCardPreviewScenario.LongTitle -> ListItemCard(
                    ui = ListItemUi(
                        id = 2L, title = "Renew the annual streaming subscription before it lapses", meta = null,
                        showCheckbox = true, checked = true, showRepeatIcon = false, badgeLabel = "Due in 10 days",
                        badgeColor = MaterialTheme.colorScheme.tertiary, checkContentDescription = "Confirm",
                    ),
                    onClick = {},
                    onToggleCheck = {},
                )
                ListItemCardPreviewScenario.SelectedInSelectionMode -> ListItemCard(
                    ui = overdueBillUi(isSelected = true),
                    onClick = {},
                    onToggleCheck = {},
                    onToggleSelection = {},
                )
                ListItemCardPreviewScenario.UnselectedInSelectionMode -> ListItemCard(
                    ui = overdueBillUi(isSelected = false),
                    onClick = {},
                    onToggleCheck = {},
                    onToggleSelection = {},
                )
            }
        }
    }
}
