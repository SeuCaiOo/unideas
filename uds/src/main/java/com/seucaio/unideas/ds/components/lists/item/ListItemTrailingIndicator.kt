package com.seucaio.unideas.ds.components.lists.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.chips.DueBadge
import com.seucaio.unideas.ds.components.lists.model.ListItemUi
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

/** Selection indicator takes over the badge's slot when [ListItemUi.isSelected] is non-null — see [ListItemUi]. */
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

private fun previewUi(isSelected: Boolean?) = ListItemUi(
    id = 1L, title = "Pay electricity bill", meta = null, showCheckbox = true,
    checked = false, showRepeatIcon = false, badgeLabel = "6 days overdue",
    badgeColor = Color.Red, checkContentDescription = "Confirm", isSelected = isSelected,
)

@PreviewLightDark
@Composable
private fun ListItemTrailingIndicatorPreview() {
    UdsTheme {
        Row(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ListItemTrailingIndicator(ui = previewUi(isSelected = null), onToggleSelection = {})
            ListItemTrailingIndicator(ui = previewUi(isSelected = true), onToggleSelection = {})
            ListItemTrailingIndicator(ui = previewUi(isSelected = false), onToggleSelection = {})
        }
    }
}
