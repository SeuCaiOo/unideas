package com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.lists.LinkedItemCard
import com.seucaio.unideas.ds.components.lists.LinkedItemUi
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel.ItemLinksEvent
import com.seucaio.unideas.feature.items.ui.screens.detail.itemlinks.viewmodel.ItemLinksUiState
import java.time.LocalDateTime

@Composable
fun ItemLinksSection(
    uiState: ItemLinksUiState,
    onEvent: (ItemLinksEvent) -> Unit,
    onAddType: (ItemType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ItemLinksSectionHeader(onAddType)

            when (uiState) {
                is ItemLinksUiState.Success -> ItemLinksSectionSuccessContent(uiState, onEvent)

                is ItemLinksUiState.Error -> ItemLinksSectionErrorContent(uiState, onEvent)

                ItemLinksUiState.Loading -> Unit
            }
        }
    }
}

@Composable
private fun ItemLinksSectionHeader(onAddType: (ItemType) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.item_links_section_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.item_links_add_content_description)
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.item_form_type_task)) },
                    onClick = {
                        menuExpanded = false
                        onAddType(ItemType.TASK)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.item_form_type_note)) },
                    onClick = {
                        menuExpanded = false
                        onAddType(ItemType.NOTE)
                    },
                )
            }
        }
    }
}

@Composable
private fun ItemLinksSectionSuccessContent(
    uiState: ItemLinksUiState.Success,
    onEvent: (ItemLinksEvent) -> Unit
) {
    if (uiState.linkedItems.isEmpty()) {
        Text(
            stringResource(R.string.item_links_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.linkedItems, key = { it.id }) { item ->
                LinkedItemCard(
                    item = item.toLinkedItemUi(),
                    onClick = { onEvent(ItemLinksEvent.OnLinkedItemClicked(item.id)) },
                    onUnlinkClicked = { onEvent(ItemLinksEvent.OnUnlinkClicked(item.id)) },
                    unlinkContentDescription = stringResource(
                        R.string.item_links_unlink_content_description
                    ),
                )
            }
        }
    }
}

@Composable
private fun ItemLinksSectionErrorContent(
    uiState: ItemLinksUiState.Error,
    onEvent: (ItemLinksEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(id = uiState.messageRes),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = { onEvent(ItemLinksEvent.OnRetryClicked) }) {
            Text(stringResource(R.string.item_links_retry))
        }
    }
}

@Composable
private fun Item.toLinkedItemUi(): LinkedItemUi = LinkedItemUi(
    id = id,
    title = title,
    typeLabel = stringResource(
        if (type == ItemType.TASK) R.string.item_form_type_task else R.string.item_form_type_note,
    ),
    isNote = type == ItemType.NOTE,
)

private fun previewItem(id: Long, title: String, type: ItemType) =
    Item(id = id, type = type, title = title, createdAt = LocalDateTime.now())

private class ItemLinksSectionPreviewProvider : PreviewParameterProvider<ItemLinksUiState> {
    override val values: Sequence<ItemLinksUiState> = sequenceOf(
        ItemLinksUiState.Success(linkedItems = listOf()),
        ItemLinksUiState.Success(
            linkedItems = listOf(
                previewItem(1L, "Comprar leite", ItemType.NOTE),
                previewItem(2L, "Revisar o PR do redesign antes de sexta", ItemType.TASK),
            ),
        ),
        ItemLinksUiState.Error(R.string.item_links_load_error),
    )
}

@PreviewLightDark
@Composable
private fun ItemLinksSectionPreview(
    @PreviewParameter(ItemLinksSectionPreviewProvider::class) previewState: ItemLinksUiState,
) {
    UdsTheme {
        ItemLinksSection(uiState = previewState, onEvent = {}, onAddType = {})
    }
}
