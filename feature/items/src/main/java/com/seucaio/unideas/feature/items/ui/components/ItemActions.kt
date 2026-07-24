package com.seucaio.unideas.feature.items.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
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

@Composable
fun ItemActions(
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
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
    }
}

@PreviewLightDark
@Composable
private fun ItemActionsPreview() {
    UdsTheme {
        Surface {
            ItemActions(
                onShareClicked = {},
                onDeleteClicked = {},
                onEditClicked = {},
            )
        }
    }
}
