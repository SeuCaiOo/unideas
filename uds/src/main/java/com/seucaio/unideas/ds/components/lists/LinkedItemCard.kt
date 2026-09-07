package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun LinkedItemCard(
    item: LinkedItemUi,
    onClick: () -> Unit,
    onUnlinkClicked: () -> Unit,
    unlinkContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radii.Chip),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.width(LinkedItemCardWidth),
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (item.isNote) Icons.Outlined.Description else Icons.Outlined.TaskAlt,
                    contentDescription = null,
                    tint = LocalUdsExtendedColors.current.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    item.typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalUdsExtendedColors.current.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onUnlinkClicked, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = unlinkContentDescription,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

data class LinkedItemUi(val id: Long, val title: String, val typeLabel: String, val isNote: Boolean)

private val LinkedItemCardWidth = 160.dp

private class LinkedItemCardPreviewProvider : PreviewParameterProvider<LinkedItemUi> {
    override val values: Sequence<LinkedItemUi> = sequenceOf(
        LinkedItemUi(id = 1L, title = "Comprar leite", typeLabel = "Nota", isNote = true),
        LinkedItemUi(
            id = 2L,
            title = "Revisar o PR do redesign antes de sexta",
            typeLabel = "Tarefa",
            isNote = false
        ),
    )
}

@PreviewLightDark
@Composable
private fun LinkedItemCardPreview(@PreviewParameter(LinkedItemCardPreviewProvider::class) item: LinkedItemUi) {
    UdsTheme {
        LinkedItemCard(
            item = item,
            onClick = {},
            onUnlinkClicked = {},
            unlinkContentDescription = "Remover vínculo",
        )
    }
}
