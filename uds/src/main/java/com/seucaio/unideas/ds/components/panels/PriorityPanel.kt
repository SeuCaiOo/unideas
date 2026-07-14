package com.seucaio.unideas.ds.components.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.LocalDsExtendedColors
import com.seucaio.unideas.ds.theme.Radii

data class PriorityRowUi(
    val id: Long,
    val title: String,
    val badgeLabel: String?,
    val badgeColor: Color
)

@Composable
fun PriorityPanel(
    title: String,
    icon: ImageVector,
    rows: List<PriorityRowUi>,
    footerLabel: String?,
    onFooterClick: () -> Unit,
    onRowClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 14.dp)
            .clip(RoundedCornerShape(Radii.Panel))
            .background(LocalDsExtendedColors.current.panelBackground)
            .border(1.dp, LocalDsExtendedColors.current.panelBorder, RoundedCornerShape(Radii.Panel))
            .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
            Text(
                title.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable { onRowClick(row.id) }
                    .padding(vertical = 9.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(row.badgeColor))
                Text(
                    row.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (row.badgeLabel != null) {
                    Text(row.badgeLabel, style = AppType.DueBadge, color = row.badgeColor)
                }
            }
        }
        if (footerLabel != null) {
            Text(
                footerLabel,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(onClick = onFooterClick)
                    .padding(vertical = 9.dp, horizontal = 2.dp)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PriorityPanelPreview() {
    DsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            PriorityPanel(
                title = "Priorities",
                icon = Icons.Outlined.Flag,
                rows = listOf(
                    PriorityRowUi(
                        id = 1L,
                        title = "Pay electricity bill",
                        badgeLabel = "6 days overdue",
                        badgeColor = MaterialTheme.colorScheme.error
                    ),
                    PriorityRowUi(
                        id = 2L,
                        title = "Morning stretch",
                        badgeLabel = "due today",
                        badgeColor = LocalDsExtendedColors.current.warning
                    )
                ),
                footerLabel = "view all (6)",
                onFooterClick = {},
                onRowClick = {}
            )
        }
    }
}
