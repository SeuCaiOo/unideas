package com.seucaio.unideas.ds.components.inputs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * A clickable header (title + chevron that rotates with [expanded]) hiding [content] behind it —
 * unlike [com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader] (list-grouping, item
 * count, pin toggle), this is a plain form/detail section with no extra affordances. State is
 * hoisted, same convention as [com.seucaio.unideas.ds.components.lists.CollapsibleGroupHeader].
 */
@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "collapsibleSectionChevron"
    )

    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = LocalUdsExtendedColors.current.textTertiary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = LocalUdsExtendedColors.current.textTertiary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column { content() }
        }
    }
}

@PreviewLightDark
@Composable
private fun CollapsibleSectionExpandedPreview() {
    UdsTheme {
        Surface {
            CollapsibleSection(title = "More options", expanded = true, onToggle = {}) {
                Text("Hidden content", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CollapsibleSectionCollapsedPreview() {
    UdsTheme {
        Surface {
            CollapsibleSection(title = "More options", expanded = false, onToggle = {}) {
                Text("Hidden content", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
