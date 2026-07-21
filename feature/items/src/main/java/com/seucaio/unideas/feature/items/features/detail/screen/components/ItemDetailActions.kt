package com.seucaio.unideas.feature.items.features.detail.screen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailEvent
import java.time.LocalDateTime

/** The Share/Edit/Delete/Complete action row shown in `ItemDetailScreen`'s top bar. */
@Composable
fun ItemDetailActions(item: Item, onEvent: (ItemDetailEvent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier) {
        IconButton(onClick = { onEvent(ItemDetailEvent.OnShareClicked) }) {
            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.item_detail_share))
        }
        IconButton(onClick = { onEvent(ItemDetailEvent.OnEditClicked) }) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.item_detail_edit))
        }
        IconButton(onClick = { onEvent(ItemDetailEvent.OnDeleteClicked) }) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.item_detail_delete))
        }
        if (item.type == ItemType.TASK && !item.isCompleted) {
            IconButton(onClick = { onEvent(ItemDetailEvent.OnCompleteClicked) }) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.item_detail_complete))
            }
        }
    }
}

private val previewItem = Item(
    type = ItemType.TASK,
    title = "Pagar contas",
    createdAt = LocalDateTime.of(2026, 6, 20, 10, 0),
)

@PreviewLightDark
@Composable
private fun ItemDetailActionsPreview() {
    UdsTheme {
        Surface {
            ItemDetailActions(item = previewItem, onEvent = {})
        }
    }
}
