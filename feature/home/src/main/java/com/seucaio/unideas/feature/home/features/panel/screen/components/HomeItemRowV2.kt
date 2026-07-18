package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.ds.components.lists.ListItemUi
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * V2 (#84) of [HomeItemRow] — same [Item]/callbacks contract, rendered via `:uds`'s native
 * `ListItemRow` (1a Tonal card row) instead of the legacy `UnideasListItem`.
 */
@Composable
fun HomeItemRowV2(
    item: Item,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkContentDescription = stringResource(R.string.home_item_recurring_content_description)
    ListItemRow(
        ui = ListItemUi(
            id = item.id,
            title = item.title,
            meta = null,
            showCheckbox = item.type == ItemType.TASK,
            checked = item.isCompleted,
            showRepeatIcon = item.dueDate != null && item.isRecurring,
            badgeLabel = dueBadgeLabel(item),
            badgeColor = dueBadgeColor(item),
            checkContentDescription = checkContentDescription,
        ),
        onClick = onClick,
        onToggleCheck = onComplete,
        modifier = modifier,
    )
}

private val previewItem = Item(
    id = 1L,
    type = ItemType.TASK,
    title = "Pagar contas",
    dueDate = LocalDate.of(2026, 7, 1),
    recurrence = Recurrence.Weekly,
    createdAt = LocalDateTime.of(2026, 6, 20, 10, 0),
)

@PreviewLightDark
@Composable
private fun HomeItemRowV2Preview() {
    UdsTheme {
        Surface {
            HomeItemRowV2(item = previewItem, onClick = {}, onComplete = {})
        }
    }
}
