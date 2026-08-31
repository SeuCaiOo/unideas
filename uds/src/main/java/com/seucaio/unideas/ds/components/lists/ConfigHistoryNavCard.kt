package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

data class NavCardConfigItem(val icon: ImageVector, val label: String)

@Composable
fun ConfigSummaryNavCard(
    title: String,
    rows: List<List<NavCardConfigItem>>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SummaryNavCard(icon = Icons.Outlined.Settings, title = title, onClick = onClick, modifier = modifier) {
        rows.forEach { row ->
            if (row.size == 1) {
                ConfigLine(row.first())
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConfigLine(row[0], modifier = Modifier.weight(1f))
                    ConfigLine(row[1])
                }
            }
        }
    }
}

@Composable
private fun ConfigLine(item: NavCardConfigItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            item.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun HistorySummaryNavCard(title: String, lines: List<String>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SummaryNavCard(icon = Icons.Outlined.History, title = title, onClick = onClick, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            lines.forEach { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryNavCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radii.Card),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = LocalUdsExtendedColors.current.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
            content()
        }
    }
}

private data class ConfigHistoryPreviewScenario(
    val configRows: List<List<NavCardConfigItem>>,
    val historyLines: List<String>,
)

private class ConfigHistoryNavCardPreviewProvider : PreviewParameterProvider<ConfigHistoryPreviewScenario> {
    override val values: Sequence<ConfigHistoryPreviewScenario> = sequenceOf(
        ConfigHistoryPreviewScenario(
            configRows = listOf(
                listOf(
                    NavCardConfigItem(Icons.Outlined.Repeat, "Diariamente"),
                    NavCardConfigItem(Icons.Outlined.Schedule, "20:00")
                ),
                listOf(
                    NavCardConfigItem(Icons.Outlined.Folder, "Casa"),
                    NavCardConfigItem(Icons.Outlined.Sell, "1 tag")
                ),
            ),
            historyLines = listOf("100% no prazo", "1 ocorrência"),
        ),
        ConfigHistoryPreviewScenario(
            configRows = listOf(
                listOf(
                    NavCardConfigItem(Icons.Outlined.Repeat, "Semanalmente, segunda-feira"),
                    NavCardConfigItem(Icons.Outlined.Schedule, "20:00"),
                ),
                listOf(
                    NavCardConfigItem(Icons.Outlined.Folder, "Seção com um nome bem grande pra testar"),
                    NavCardConfigItem(Icons.Outlined.Sell, "3 tags"),
                ),
            ),
            historyLines = listOf("0% no prazo", "12 ocorrências"),
        ),
    )
}

@PreviewLightDark
@Composable
private fun ConfigHistoryNavCardsPreview(
    @PreviewParameter(ConfigHistoryNavCardPreviewProvider::class) scenario: ConfigHistoryPreviewScenario,
) {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HistorySummaryNavCard(title = "Histórico", lines = scenario.historyLines, onClick = {})
                ConfigSummaryNavCard(title = "Configurações", rows = scenario.configRows, onClick = {})
            }
        }
    }
}
