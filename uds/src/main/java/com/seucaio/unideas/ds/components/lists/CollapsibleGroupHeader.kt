package com.seucaio.unideas.ds.components.lists

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * A clickable, collapsible variant of [GroupHeader] — title (+ item count) with a chevron that
 * rotates to reflect [expanded]. Used to group a lazy list's rows by section, expand/collapse per
 * group.
 */
@Composable
fun CollapsibleGroupHeader(
    title: String,
    itemCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevronRotation")

    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "${title.uppercase()} ($itemCount)",
            style = AppType.FieldLabel,
            color = LocalUdsExtendedColors.current.textTertiary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = LocalUdsExtendedColors.current.textTertiary,
            modifier = Modifier.size(18.dp).rotate(rotation),
        )
    }
}

@PreviewLightDark
@Composable
private fun CollapsibleGroupHeaderExpandedPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            CollapsibleGroupHeader(title = "Work", itemCount = 4, expanded = true, onToggle = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun CollapsibleGroupHeaderCollapsedPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            CollapsibleGroupHeader(title = "Personal", itemCount = 12, expanded = false, onToggle = {})
        }
    }
}
