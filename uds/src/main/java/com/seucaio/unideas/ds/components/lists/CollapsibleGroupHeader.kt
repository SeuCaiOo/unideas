package com.seucaio.unideas.ds.components.lists

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.pinnedBackgroundColor
import com.seucaio.unideas.ds.theme.pinnedContainerColor

/**
 * A clickable, collapsible variant of [GroupHeader] — title (+ item count) with a chevron that
 * rotates to reflect [expanded]. Used to group a lazy list's rows by section, expand/collapse per
 * group. [onTogglePin], when non-null, renders a pin toggle (filled when [isPinned]) — pinned
 * groups sort first in the caller's list. [indentStart], on top of the base start padding, reads
 * this group as nested under a meta-group divider above it (e.g. an emphasized [GroupHeader]
 * "Pinned") instead of a sibling at the same level.
 */
@Composable
fun CollapsibleGroupHeader(
    title: String,
    itemCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null,
    indentStart: Dp = 0.dp,
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevronRotation")
    val labelColor = if (isPinned) MaterialTheme.colorScheme.primary else LocalUdsExtendedColors.current.textTertiary

    Row(
        modifier
            .fillMaxWidth()
            .background(pinnedBackgroundColor(isPinned))
            .clickable(onClick = onToggle)
            .padding(start = 20.dp + indentStart, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "${title.uppercase()} ($itemCount)",
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            modifier = Modifier.weight(1f),
        )
        if (onTogglePin != null) {
            IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = labelColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
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

@PreviewLightDark
@Composable
private fun CollapsibleGroupHeaderPinnedPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            CollapsibleGroupHeader(
                title = "Work",
                itemCount = 4,
                expanded = true,
                onToggle = {},
                isPinned = true,
                onTogglePin = {},
            )
        }
    }
}

/**
 * End-to-end shape of a pinned group as `:feature:home` actually renders it — the emphasized
 * "Pinned" [GroupHeader] divider, an indented pinned [CollapsibleGroupHeader], and a pinned
 * [ListItemRow] below it, all in one glance instead of three separate previews the reader has to
 * mentally stack.
 */
@PreviewLightDark
@Composable
private fun PinnedGroupEndToEndPreview() {
    UdsTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            GroupHeader("Pinned", emphasized = true)
            CollapsibleGroupHeader(
                title = "Work",
                itemCount = 2,
                expanded = true,
                onToggle = {},
                isPinned = true,
                onTogglePin = {},
                indentStart = 12.dp,
            )
            ListItemRow(
                ui = ListItemUi(
                    id = 1L,
                    title = "Pay electricity bill",
                    meta = "Home",
                    showCheckbox = true,
                    checked = false,
                    showRepeatIcon = true,
                    badgeLabel = "6 days overdue",
                    badgeColor = MaterialTheme.colorScheme.error,
                    checkContentDescription = "Confirm",
                ),
                onClick = {},
                onToggleCheck = {},
                containerColor = pinnedContainerColor(isPinned = true, base = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
