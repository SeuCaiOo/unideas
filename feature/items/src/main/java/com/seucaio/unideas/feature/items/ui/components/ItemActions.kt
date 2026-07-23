package com.seucaio.unideas.feature.items.ui.components

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
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

/**
 * The Share/Delete/Complete action row shared by every screen backed by an item (currently
 * `ItemDetailScreen` and `ItemScreen`). Takes plain callbacks instead of a ViewModel's own event
 * type — neither `ItemFormViewModel` nor `ItemDetailViewModel` (still separate, #97) needs to
 * know the other exists for this to be reused; each caller just adapts its own event to these
 * lambdas. [canComplete] is the "task, not yet completed" check each ViewModel's state already
 * exposes differently (`ItemFormUiState.typeIsTask && !isCompleted` vs
 * `item.type == ItemType.TASK && !item.isCompleted`), so it's passed in already resolved.
 * [onEditClicked] is optional and `null` by default — only `ItemDetailScreen` still has a
 * separate "editar" destination to jump to; `ItemScreen` is already editable, so it omits it.
 */
@Composable
fun ItemActions(
    canComplete: Boolean,
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onEditClicked: (() -> Unit)? = null,
) {
    Row(modifier) {
        IconButton(onClick = onShareClicked) {
            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.item_detail_share))
        }
        if (onEditClicked != null) {
            IconButton(onClick = onEditClicked) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.item_detail_edit))
            }
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.item_detail_delete))
        }
        if (canComplete) {
            IconButton(onClick = onCompleteClicked) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.item_detail_complete))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemActionsPreview() {
    UdsTheme {
        Surface {
            ItemActions(
                canComplete = true,
                onShareClicked = {},
                onDeleteClicked = {},
                onCompleteClicked = {},
                onEditClicked = {},
            )
        }
    }
}
