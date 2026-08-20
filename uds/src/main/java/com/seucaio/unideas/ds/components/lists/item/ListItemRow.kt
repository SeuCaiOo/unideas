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
import com.seucaio.unideas.ds.theme.leftAccentBorder

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
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(containerColor)
            .leftAccentBorder(3.dp, accentColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 13.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (ui.showCheckbox) {
            ListItemCheckbox(ui.checked, ui.checkContentDescription, onToggleCheck)
        }
        Column(Modifier.weight(1f)) {
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
            if (!ui.description.isNullOrBlank()) {
                Text(
                    ui.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(
                Modifier.padding(top = 6.dp),
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
        if (ui.isSelected != null) {
            SelectionIndicator(ui.isSelected, onToggle = { onToggleSelection?.invoke() })
        } else {
            NormalTrailingContent(ui, onTogglePin)
        }
    }
}

private enum class ListItemRowPreviewScenario {
    Default, Pinned, WithDescription, SelectedInSelectionMode, UnselectedInSelectionMode
}

private class ListItemRowPreviewProvider : PreviewParameterProvider<ListItemRowPreviewScenario> {
    override val values = ListItemRowPreviewScenario.entries.asSequence()
}

@Composable
private fun overdueBillRow(
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
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            when (scenario) {
                ListItemRowPreviewScenario.Default -> overdueBillRow()
                ListItemRowPreviewScenario.Pinned -> overdueBillRow(isPinned = true)
                ListItemRowPreviewScenario.WithDescription -> overdueBillRow(
                    description = "Check the meter reading before paying — last month's bill looked off.",
                )
                ListItemRowPreviewScenario.SelectedInSelectionMode -> overdueBillRow(isSelected = true)
                ListItemRowPreviewScenario.UnselectedInSelectionMode -> overdueBillRow(isSelected = false)
            }
        }
    }
}
