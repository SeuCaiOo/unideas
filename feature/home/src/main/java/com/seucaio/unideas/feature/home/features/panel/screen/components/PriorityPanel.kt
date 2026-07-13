package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.feature.home.R
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The fixed panel at the top of Home — overdue + due-soon [items], independent of the active
 * tab. Not Home-specific in its own right (no [com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeEvent]
 * dependency, plain callbacks instead), just the screen area that happens to live there.
 */
@Composable
fun PriorityPanel(
    items: List<Item>,
    showSeeAllButton: Boolean,
    onItemClick: (Long) -> Unit,
    onItemComplete: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = stringResource(R.string.home_panel_title), style = MaterialTheme.typography.titleMedium)
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.home_panel_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                items.forEach { item ->
                    HomeItemRow(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onComplete = { onItemComplete(item.id) },
                    )
                }
            }
        }
        if (showSeeAllButton) {
            Button(onClick = onSeeAllClick, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.home_see_all))
            }
        }
    }
}

private val previewItems = listOf(
    Item(
        id = 1L,
        type = ItemType.TASK,
        title = "Pagar contas",
        dueDate = LocalDate.of(2026, 6, 25),
        createdAt = LocalDateTime.of(2026, 6, 20, 10, 0),
    ),
)

@PreviewLightDark
@Composable
private fun PriorityPanelPreview() {
    UnideasTheme {
        Surface {
            PriorityPanel(
                items = previewItems,
                showSeeAllButton = true,
                onItemClick = {},
                onItemComplete = {},
                onSeeAllClick = {},
            )
        }
    }
}
