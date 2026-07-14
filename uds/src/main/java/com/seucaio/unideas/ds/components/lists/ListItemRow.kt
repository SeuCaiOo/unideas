package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.chips.DueBadge
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.LocalDsExtendedColors
import com.seucaio.unideas.ds.theme.Radii

data class ListItemUi(
    val id: Long,
    val title: String,
    val meta: String?,
    val showCheckbox: Boolean,
    val checked: Boolean,
    val showRepeatIcon: Boolean,
    val badgeLabel: String?,
    val badgeColor: Color,
    val checkContentDescription: String
)

@Composable
fun ListItemRow(ui: ListItemUi, onClick: () -> Unit, onToggleCheck: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (ui.showCheckbox) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(Radii.Checkbox))
                    .background(if (ui.checked) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(
                        if (ui.checked) 0.dp else 2.dp,
                        LocalDsExtendedColors.current.textTertiary,
                        RoundedCornerShape(Radii.Checkbox)
                    )
                    .clickable(onClick = onToggleCheck),
                contentAlignment = Alignment.Center
            ) {
                if (ui.checked) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = ui.checkContentDescription,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                ui.title,
                style = AppType.ListItemTitle,
                color = if (ui.checked) {
                    LocalDsExtendedColors.current.textTertiary
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
                    Text(ui.meta, style = AppType.Metadata, color = LocalDsExtendedColors.current.textTertiary)
                }
                if (ui.showRepeatIcon) {
                    Icon(
                        Icons.Outlined.Repeat,
                        contentDescription = null,
                        tint = LocalDsExtendedColors.current.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        if (ui.badgeLabel != null) {
            DueBadge(label = ui.badgeLabel, color = ui.badgeColor)
        }
    }
}

@PreviewLightDark
@Composable
private fun ListItemRowPreview() {
    DsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            ListItemRow(
                ui = ListItemUi(
                    id = 1L, title = "Pay electricity bill", meta = "Home", showCheckbox = true,
                    checked = false, showRepeatIcon = true, badgeLabel = "6 days overdue",
                    badgeColor = MaterialTheme.colorScheme.error, checkContentDescription = "Confirm"
                ),
                onClick = {},
                onToggleCheck = {}
            )
        }
    }
}
