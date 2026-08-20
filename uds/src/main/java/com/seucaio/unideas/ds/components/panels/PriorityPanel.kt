package com.seucaio.unideas.ds.components.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.components.chips.DueBadge
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.leftAccentBorder

data class PriorityRowUi(
    val id: Long,
    val title: String,
    val badgeLabel: String?,
    val badgeColor: Color
)

@Composable
fun PriorityPanel(
    title: String,
    rows: List<PriorityRowUi>,
    footerLabel: String?,
    onFooterClick: () -> Unit,
    onRowClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    emptyText: String = ""
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (rows.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                rows.forEach { row ->
                    PriorityRow(
                        row = row,
                        onClick = { onRowClick(row.id) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                if (footerLabel != null) {
                    Text(
                        "$footerLabel →",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onFooterClick)
                            .padding(top = 14.dp),
                    )
                }
            }
        }
    }
}

private const val PRIORITY_ROW_BACKGROUND_ALPHA = 0.08f

@Composable
fun PriorityRow(row: PriorityRowUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accentColor = if (row.badgeLabel != null) row.badgeColor else Color.Transparent
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(accentColor.copy(alpha = PRIORITY_ROW_BACKGROUND_ALPHA))
            .leftAccentBorder(4.dp, accentColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
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
            DueBadge(label = row.badgeLabel, color = row.badgeColor)
        }
    }
}

private fun samplePriorityRows(errorColor: Color, warningColor: Color, neutralColor: Color) = listOf(
    PriorityRowUi(id = 1L, title = "Pay electricity bill", badgeLabel = "6 days overdue", badgeColor = errorColor),
    PriorityRowUi(id = 2L, title = "Morning stretch", badgeLabel = "due today", badgeColor = warningColor),
    PriorityRowUi(
        id = 3L,
        title = "Renew streaming subscription",
        badgeLabel = "due in 12 days",
        badgeColor = neutralColor,
    ),
)

private enum class PriorityPanelPreviewScenario { WithRows, Empty }

private class PriorityPanelPreviewProvider : PreviewParameterProvider<PriorityPanelPreviewScenario> {
    override val values = PriorityPanelPreviewScenario.entries.asSequence()
}

@PreviewLightDark
@Composable
private fun PriorityPanelPreview(
    @PreviewParameter(PriorityPanelPreviewProvider::class) scenario: PriorityPanelPreviewScenario,
) {
    UdsTheme {
        when (scenario) {
            PriorityPanelPreviewScenario.WithRows -> PriorityPanel(
                title = "Priorities",
                rows = samplePriorityRows(
                    errorColor = MaterialTheme.colorScheme.error,
                    warningColor = MaterialTheme.colorScheme.secondary,
                    neutralColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                footerLabel = "view all (6)",
                onFooterClick = {},
                onRowClick = {},
            )
            PriorityPanelPreviewScenario.Empty -> PriorityPanel(
                title = "Priorities",
                rows = emptyList(),
                footerLabel = null,
                onFooterClick = {},
                onRowClick = {},
                emptyText = "No priorities",
            )
        }
    }
}
