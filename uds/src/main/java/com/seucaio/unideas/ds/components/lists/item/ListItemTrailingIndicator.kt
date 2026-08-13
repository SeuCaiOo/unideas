package com.seucaio.unideas.ds.components.lists.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.chips.DueBadge
import com.seucaio.unideas.ds.components.lists.model.ListItemUi
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
internal fun ListItemTrailingIndicator(ui: ListItemUi, onToggleSelection: (() -> Unit)?, size: Dp = 24.dp) {
    if (ui.isSelected != null) {
        Icon(
            if (ui.isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (ui.isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalUdsExtendedColors.current.textTertiary
            },
            modifier = Modifier
                .size(size)
                .clickable(onClick = { onToggleSelection?.invoke() }),
        )
    } else if (ui.badgeLabel != null) {
        DueBadge(label = ui.badgeLabel, color = ui.badgeColor)
    }
}

@Composable
internal fun SelectionIndicator(isSelected: Boolean, onToggle: () -> Unit, size: Dp = 24.dp) {
    Icon(
        if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
        contentDescription = null,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else LocalUdsExtendedColors.current.textTertiary,
        modifier = Modifier
            .size(size)
            .clickable(onClick = onToggle),
    )
}

@Composable
internal fun NormalTrailingContent(
    ui: ListItemUi,
    onTogglePin: (() -> Unit)?,
    modifier: Modifier = Modifier,
    pinButtonSize: Dp = 32.dp,
    pinIconSize: Dp = 18.dp,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (ui.badgeLabel != null) {
            DueBadge(label = ui.badgeLabel, color = ui.badgeColor)
        }
        if (onTogglePin != null) {
            IconButton(onClick = onTogglePin, modifier = Modifier.size(pinButtonSize)) {
                Icon(
                    if (ui.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = if (ui.isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalUdsExtendedColors.current.textTertiary
                    },
                    modifier = Modifier.size(pinIconSize),
                )
            }
        }
    }
}

private fun previewUi(isPinned: Boolean = false) = ListItemUi(
    id = 1L, title = "Pay electricity bill", meta = null, showCheckbox = false,
    checked = false, showRepeatIcon = false, badgeLabel = "6 days overdue",
    badgeColor = Color.Red, checkContentDescription = "Confirm", isPinned = isPinned,
)

@Composable
private fun PreviewRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(Radii.Card))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun RowScope.PreviewTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
    )
}

@PreviewLightDark
@Composable
private fun ListItemTrailingContentPreview() {
    UdsTheme {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PreviewRow {
                PreviewTitle(previewUi().title)
                NormalTrailingContent(ui = previewUi(), onTogglePin = {})
            }
            PreviewRow {
                PreviewTitle(previewUi().title)
                NormalTrailingContent(ui = previewUi(isPinned = true), onTogglePin = {})
            }
            PreviewRow {
                PreviewTitle(previewUi().title)
                NormalTrailingContent(ui = previewUi(), onTogglePin = null)
            }
            PreviewRow {
                PreviewTitle(previewUi().title)
                SelectionIndicator(isSelected = true, onToggle = {})
            }
            PreviewRow {
                PreviewTitle(previewUi().title)
                SelectionIndicator(isSelected = false, onToggle = {})
            }
        }
    }
}
